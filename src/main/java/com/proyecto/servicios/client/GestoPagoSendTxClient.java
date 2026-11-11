package com.proyecto.servicios.client;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.ProveedorComunicacionException;
import com.proyecto.servicios.model.gestopago.SendTxRequestDto;
import com.proyecto.servicios.model.gestopago.SendTxResponseDto;
import com.proyecto.servicios.service.GestoPagoTokenService;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.Optional;

@Slf4j
@Component
public class GestoPagoSendTxClient {

    private RestTemplate restTemplate;

    @Value("${gestopago.auth.url:https://gestopago.portalventas.net}")
    private String baseUrl;

    @Value("${gestopago.service.sendTx:/sistema/service/sendTx.do}")
    private String sendTxPath;

    @Value("${gestopago.auth.id-distribuidor:83}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:GPS83-TPV-17}")
    private String codigoDispositivo;

    @Value("${external.service.token:}")
    private String fallbackToken;

    @Value("${gestopago.http.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${gestopago.http.read-timeout-ms:15000}")
    private int readTimeoutMs;

    @Autowired
    private GestoPagoTokenService gestoPagoTokenService;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    public SendTxResponseDto enviarTransaccion(SendTxRequestDto requestDto) {
        String url = baseUrl + sendTxPath;
        String bearerToken = resolverTokenSeguro();

        log.info("Enviando transacción local {} a proveedor GestoPago para servicio={} producto={}",
                requestDto.getTransaccionLocalId(), requestDto.getIdServicio(), requestDto.getIdProducto());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(MediaType.parseMediaTypes("application/xml, text/xml, application/json"));

            MultiValueMap<String, String> formBody = new LinkedMultiValueMap<>();
            formBody.add("idServicio", requestDto.getIdServicio());
            formBody.add("idProducto", requestDto.getIdProducto());
            formBody.add("referencia", requestDto.getReferencia());
            formBody.add("montoPago", requestDto.getMonto().toPlainString());
            formBody.add("idTransaccion", requestDto.getTransaccionLocalId());
            if (requestDto.getTelefono() != null && !requestDto.getTelefono().isBlank()) {
                formBody.add("telefono", requestDto.getTelefono());
            }

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parsearRespuestaProveedor(response.getBody());
            }

            return SendTxResponseDto.builder()
                    .codigo(String.valueOf(response.getStatusCode().value()))
                    .mensaje("El proveedor devolvió un estado no exitoso.")
                    .aprobada(false)
                    .rechazoDefinitivo(response.getStatusCode().is4xxClientError())
                    .estadoIndeterminado(response.getStatusCode().is5xxServerError())
                    .build();

        } catch (ResourceAccessException e) {
            log.warn("Timeout o interrupción de red en sendTx para transacción local {}. Se requiere conciliación.",
                    requestDto.getTransaccionLocalId());
            return SendTxResponseDto.builder()
                    .codigo("TIMEOUT")
                    .mensaje("Tiempo de espera agotado al comunicar con el proveedor. La operación quedó pendiente de conciliación.")
                    .aprobada(false)
                    .rechazoDefinitivo(false)
                    .estadoIndeterminado(true)
                    .build();
        } catch (HttpServerErrorException e) {
            log.warn("Error 5xx del proveedor en sendTx para transacción local {} (HTTP {}). Se requiere conciliación.",
                    requestDto.getTransaccionLocalId(), e.getStatusCode().value());
            return SendTxResponseDto.builder()
                    .codigo("HTTP_" + e.getStatusCode().value())
                    .mensaje("El proveedor experimentó una falla temporal al procesar la solicitud. Operación en conciliación.")
                    .aprobada(false)
                    .rechazoDefinitivo(false)
                    .estadoIndeterminado(true)
                    .build();
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.error("Rechazo de autorización HTTP {} con proveedor externo en transacción local {}",
                    e.getStatusCode().value(), requestDto.getTransaccionLocalId());
            return SendTxResponseDto.builder()
                    .codigo("AUTH_ERROR")
                    .mensaje("No fue posible autenticar la operación con el proveedor de servicios.")
                    .aprobada(false)
                    .rechazoDefinitivo(true)
                    .estadoIndeterminado(false)
                    .build();
        } catch (HttpClientErrorException e) {
            log.warn("Rechazo 4xx del proveedor en sendTx para transacción local {} (HTTP {})",
                    requestDto.getTransaccionLocalId(), e.getStatusCode().value());
            return SendTxResponseDto.builder()
                    .codigo("HTTP_" + e.getStatusCode().value())
                    .mensaje("El proveedor rechazó los parámetros de la operación.")
                    .aprobada(false)
                    .rechazoDefinitivo(true)
                    .estadoIndeterminado(false)
                    .build();
        } catch (Exception e) {
            log.error("Falla al procesar respuesta del proveedor en transacción local {}", requestDto.getTransaccionLocalId());
            return SendTxResponseDto.builder()
                    .codigo("INDETERMINADO")
                    .mensaje("No fue posible confirmar el estado final de la operación con el proveedor.")
                    .aprobada(false)
                    .rechazoDefinitivo(false)
                    .estadoIndeterminado(true)
                    .build();
        }
    }

    private String resolverTokenSeguro() {
        Optional<GestoPagoToken> tokenBd = gestoPagoTokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo);
        if (tokenBd.isPresent() && tokenBd.get().getToken() != null && !tokenBd.get().getToken().isBlank()) {
            return tokenBd.get().getToken().trim();
        }
        if (fallbackToken != null && !fallbackToken.isBlank()) {
            return fallbackToken.trim();
        }
        throw new ProveedorComunicacionException("No existe un token Bearer activo configurado para comunicarse con el proveedor de pagos.");
    }

    private SendTxResponseDto parsearRespuestaProveedor(String rawBody) {
        String trimmed = rawBody.trim();
        try {
            if (trimmed.startsWith("<")) {
                JAXBContext context = JAXBContext.newInstance(SendTxResponseDto.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();

                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

                XMLReader xmlReader = spf.newSAXParser().getXMLReader();
                InputSource inputSource = new InputSource(new StringReader(trimmed));
                SAXSource source = new SAXSource(xmlReader, inputSource);

                SendTxResponseDto parsed = (SendTxResponseDto) unmarshaller.unmarshal(source);
                evaluarCodigoProveedor(parsed);
                return parsed;
            }
        } catch (Exception e) {
            log.warn("No se pudo deserializar XML estándar de sendTx, evaluando respuesta de proveedor.");
        }

        boolean exito = trimmed.contains("\"codigo\":\"0\"")
                || trimmed.contains("\"codigo\":\"00\"")
                || trimmed.contains("<CODIGO>0</CODIGO>")
                || trimmed.contains("<CODIGO>00</CODIGO>")
                || trimmed.contains("<CODIGO>01</CODIGO>");

        return SendTxResponseDto.builder()
                .codigo(exito ? "00" : "RECHAZO")
                .mensaje(exito ? "Transacción procesada exitosamente por el proveedor." : "Transacción rechazada por el proveedor.")
                .numAutorizacion(exito ? "AUT-" + System.currentTimeMillis() : null)
                .aprobada(exito)
                .rechazoDefinitivo(!exito)
                .estadoIndeterminado(false)
                .build();
    }

    private void evaluarCodigoProveedor(SendTxResponseDto dto) {
        String cod = dto.getCodigo() != null ? dto.getCodigo().trim() : "";
        boolean aprobada = "0".equals(cod) || "00".equals(cod) || "01".equals(cod) || "200".equals(cod) || "OK".equalsIgnoreCase(cod);
        dto.setAprobada(aprobada);
        dto.setRechazoDefinitivo(!aprobada);
        dto.setEstadoIndeterminado(false);
    }
}

