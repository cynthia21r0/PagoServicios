package com.proyecto.servicios.service.pago;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.config.GestoPagoTxProperties;
import com.proyecto.servicios.exception.ProveedorNoDisponibleException;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxResponse;
import com.proyecto.servicios.util.GestoPagoCryptoUtil;
import feign.FeignException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class GestoPagoGateway {

    static final String CODIGO_EXITO = "01";
    private static final JAXBContext JAXB_CONTEXT = crearJaxbContext();
    private static final XMLInputFactory XML_FACTORY = crearXmlFactory();
    private static final ZoneId ZONA_MX = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter HORA_LOCAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final GestoPagoTxClient client;
    private final GestoPagoCryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;
    private final GestoPagoTxProperties properties;
    private final Integer idDistribuidor;

    public GestoPagoGateway(GestoPagoTxClient client,
                            GestoPagoCryptoUtil cryptoUtil,
                            ObjectMapper objectMapper,
                            GestoPagoTxProperties properties,
                            @Value("${gestopago.auth.id-distribuidor}") Integer idDistribuidor) {
        this.client = client;
        this.cryptoUtil = cryptoUtil;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.idDistribuidor = idDistribuidor;
    }

    public String generarUpc() {
        long timestampParte = System.currentTimeMillis() % 1000000000L;
        int randomTres = (int) (ThreadLocalRandom.current().nextDouble() * 900) + 100;
        return String.format("%09d%03d", timestampParte, randomTres);
    }

    public String construirPayload(PagoPlan plan, String upc) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("idProducto", Integer.parseInt(plan.producto().getIdProducto()));
        payload.put("idServicio", Integer.parseInt(plan.producto().getIdServicio()));
        payload.put("telefono", plan.telefono());
        payload.put("horaLocal", ZonedDateTime.now(ZONA_MX).format(HORA_LOCAL));
        if (plan.referencia() != null) {
            payload.put("referencia", plan.referencia());
        }
        if (plan.montoAbierto()) {
            payload.put("montoPago", plan.monto());
        }
        payload.put("upc", upc);
        payload.put("unidad", properties.getUnidad());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No fue posible serializar el payload de la transaccion", e);
        }
    }

    public ResultadoProveedor enviar(String payloadJson, String upc) {
        String signed;
        try {
            signed = cryptoUtil.encrypt(payloadJson);
        } catch (RuntimeException e) {
            log.error("No fue posible cifrar el payload, upc={}", upc);
            return ResultadoProveedor.noEnviado("ERROR_CIFRADO");
        }
        String random = String.valueOf(System.currentTimeMillis());

        ResponseEntity<String> respuesta;
        try {
            respuesta = client.sendTx(properties.getApiKey(), random, signed);
        } catch (ProveedorNoDisponibleException e) {
            log.error("sendTx no enviado, upc={}: {}", upc, e.getMessage());
            return ResultadoProveedor.noEnviado("SIN_TOKEN");
        } catch (FeignException e) {
            return clasificarFalloFeign(e, upc);
        } catch (Exception e) {
            if (nuncaLlegoAlProveedor(e)) {
                log.error("sendTx sin conexion con el proveedor, upc={}", upc);
                return ResultadoProveedor.noEnviado("SIN_CONEXION");
            }
            log.error("sendTx fallo con resultado desconocido, upc={}, tipo={}", upc, e.getClass().getSimpleName());
            return ResultadoProveedor.incierto("ERROR_DESCONOCIDO", e.getClass().getSimpleName());
        }

        return interpretar(respuesta, upc);
    }

    private ResultadoProveedor clasificarFalloFeign(FeignException e, String upc) {
        int status = e.status();
        if (status >= 400 && status < 500) {
            log.error("sendTx rechazado por HTTP {}, upc={}", status, upc);
            return ResultadoProveedor.noEnviado("HTTP_" + status);
        }
        if (status >= 500) {
            log.error("sendTx respondio HTTP {}, upc={}: resultado incierto", status, upc);
            return ResultadoProveedor.incierto("HTTP_" + status, "Error del servidor del proveedor");
        }
        if (nuncaLlegoAlProveedor(e)) {
            log.error("sendTx sin conexion con el proveedor, upc={}", upc);
            return ResultadoProveedor.noEnviado("SIN_CONEXION");
        }
        log.error("sendTx sin respuesta (timeout o corte), upc={}: resultado incierto", upc);
        return ResultadoProveedor.incierto("TIMEOUT", "Sin respuesta del proveedor");
    }

    private static boolean nuncaLlegoAlProveedor(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof ConnectException || t instanceof UnknownHostException || t instanceof NoRouteToHostException) {
                return true;
            }
            if (t instanceof SocketTimeoutException && t.getMessage() != null
                    && t.getMessage().toLowerCase(Locale.ROOT).contains("connect")) {
                return true;
            }
            if (t instanceof java.net.http.HttpConnectTimeoutException) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }

    private ResultadoProveedor interpretar(ResponseEntity<String> respuesta, String upc) {
        if (respuesta == null || respuesta.getBody() == null || respuesta.getBody().isBlank()) {
            log.error("sendTx devolvio una respuesta vacia, upc={}: resultado incierto", upc);
            return ResultadoProveedor.incierto("RESPUESTA_VACIA", "El proveedor respondio sin contenido");
        }

        GestoPagoTxResponse parsed;
        try {
            parsed = deserializarXml(respuesta.getBody());
        } catch (JAXBException | XMLStreamException | RuntimeException e) {
            log.error("sendTx devolvio un cuerpo ilegible, upc={}: resultado incierto", upc);
            return ResultadoProveedor.incierto("RESPUESTA_ILEGIBLE", "No fue posible interpretar la respuesta");
        }

        String codigo = parsed.getMensaje() != null && parsed.getMensaje().getCodigo() != null
                ? parsed.getMensaje().getCodigo().trim() : null;
        String texto = parsed.getMensaje() != null ? parsed.getMensaje().getTexto() : null;
        if (codigo == null || codigo.isEmpty()) {
            log.error("sendTx devolvio una respuesta sin codigo, upc={}: resultado incierto", upc);
            return ResultadoProveedor.incierto("SIN_CODIGO", "La respuesta no trae codigo de operacion");
        }
        if (CODIGO_EXITO.equals(codigo)) {
            return ResultadoProveedor.exito(codigo, texto, parsed.getIdTx(), parsed.getNumAutorizacion());
        }
        if (properties.getCodigosPendientes().contains(codigo)) {
            log.warn("sendTx devolvio codigo en proceso {}, upc={}", codigo, upc);
            return ResultadoProveedor.incierto(codigo, texto);
        }
        if (properties.isMockSandbox()) {
            log.info("Mock sandbox activo: simulando aprobacion exitosa para codigo rechazo {} en upc={}", codigo, upc);
            String mockIdTx = parsed.getIdTx() != null ? parsed.getIdTx() : String.valueOf(System.currentTimeMillis() % 10000000);
            String mockAuth = parsed.getNumAutorizacion() != null ? parsed.getNumAutorizacion() : "AUTH-" + (System.currentTimeMillis() % 100000);
            return ResultadoProveedor.exito(CODIGO_EXITO, "Aprobada (Simulacion Sandbox)", mockIdTx, mockAuth);
        }
        log.warn("sendTx rechazado por el proveedor, codigo={}, upc={}", codigo, upc);
        return ResultadoProveedor.rechazo(codigo, texto, parsed.getIdTx(), parsed.getNumAutorizacion());
    }

    private static GestoPagoTxResponse deserializarXml(String xml) throws JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        XMLStreamReader reader = XML_FACTORY.createXMLStreamReader(new StringReader(xml));
        try {
            return (GestoPagoTxResponse) unmarshaller.unmarshal(reader);
        } finally {
            reader.close();
        }
    }

    private static XMLInputFactory crearXmlFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    private static JAXBContext crearJaxbContext() {
        try {
            return JAXBContext.newInstance(GestoPagoTxResponse.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}