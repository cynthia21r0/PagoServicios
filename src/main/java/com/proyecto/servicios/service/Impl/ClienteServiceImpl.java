package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Domicilio;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.security.BCryptPasswordEncoder;
import com.proyecto.servicios.service.ClienteService;
import com.proyecto.servicios.util.ValidadorNegocio;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${onboarding.cuenta.saldo-inicial:1000.00}")
    private BigDecimal saldoInicialSistema;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public ClienteResponse registrarCliente(ClienteRegistroRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de registro del cliente son obligatorios.");
        }

        if (request.getId() != null || request.getFechaCreacion() != null || request.getFechaActualizacion() != null) {
            throw new ValidacionException("No está permitido asignar manualmente campos controlados por el servidor (id, fechaCreacion, fechaActualizacion).");
        }

        ValidadorNegocio.validarNombreOApellido(request.getNombre(), "nombre", true);
        ValidadorNegocio.validarNombreOApellido(request.getSegundoNombre(), "segundoNombre", false);
        ValidadorNegocio.validarNombreOApellido(request.getApellidoPaterno(), "apellidoPaterno", true);
        ValidadorNegocio.validarNombreOApellido(request.getApellidoMaterno(), "apellidoMaterno", true);
        ValidadorNegocio.validarFechaNacimiento(request.getFechaNacimiento());
        ValidadorNegocio.validarCurp(request.getCurp());
        ValidadorNegocio.validarRfc(request.getRfc());
        ValidadorNegocio.validarCampoObligatorio(request.getSexo(), "sexo");
        ValidadorNegocio.validarCampoObligatorio(request.getNacionalidad(), "nacionalidad");
        ValidadorNegocio.validarCampoObligatorio(request.getEstadoCivil(), "estadoCivil");

        ValidadorNegocio.validarCorreo(request.getCorreoElectronico());
        ValidadorNegocio.validarTelefono(request.getTelefonoMovil(), "telefonoMovil", true);
        ValidadorNegocio.validarTelefono(request.getTelefonoAlternativo(), "telefonoAlternativo", false);

        DomicilioDto domInput = extraerDomicilio(request);
        if (domInput.getId() != null) {
            throw new ValidacionException("No está permitido especificar el identificador interno del domicilio.");
        }
        ValidadorNegocio.validarCampoObligatorio(domInput.getCalle(), "calle");
        ValidadorNegocio.validarCampoObligatorio(domInput.getNumeroExterior(), "numeroExterior");
        ValidadorNegocio.validarCampoObligatorio(domInput.getColonia(), "colonia");
        ValidadorNegocio.validarCampoObligatorio(domInput.getMunicipio(), "municipio");
        ValidadorNegocio.validarCampoObligatorio(domInput.getEstado(), "estado");
        ValidadorNegocio.validarCodigoPostal(domInput.getCodigoPostal());
        ValidadorNegocio.validarCampoObligatorio(domInput.getPais(), "pais");

        ValidadorNegocio.validarCampoObligatorio(request.getOcupacion(), "ocupacion");
        ValidadorNegocio.validarCampoObligatorio(request.getEmpresa(), "empresa");
        ValidadorNegocio.validarIngresoMensual(request.getIngresoMensual());

        ValidadorNegocio.validarPassword(request.getPassword());

        BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial() : saldoInicialSistema;
        ValidadorNegocio.validarSaldoCuenta(saldoInicial);

        String curpLimpia = request.getCurp().trim().toUpperCase();
        String rfcLimpio = request.getRfc().trim().toUpperCase();
        String correoLimpio = request.getCorreoElectronico().trim().toLowerCase();

        if (clienteRepository.existsByCurpIgnoreCase(curpLimpia)) {
            throw new CurpDuplicadaException("Ya existe un cliente registrado con la CURP: " + curpLimpia);
        }
        if (clienteRepository.existsByRfcIgnoreCase(rfcLimpio)) {
            throw new RfcDuplicadoException("Ya existe un cliente registrado con el RFC: " + rfcLimpio);
        }
        if (clienteRepository.existsByCorreoElectronicoIgnoreCase(correoLimpio)
                || usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new CorreoDuplicadoException("Ya existe un cliente o usuario registrado con el correo electrónico: " + correoLimpio);
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre().trim());
        cliente.setSegundoNombre(request.getSegundoNombre() != null && !request.getSegundoNombre().trim().isEmpty()
                ? request.getSegundoNombre().trim() : null);
        cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        cliente.setApellidoMaterno(request.getApellidoMaterno().trim());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setCurp(curpLimpia);
        cliente.setRfc(rfcLimpio);
        cliente.setSexo(request.getSexo().trim());
        cliente.setNacionalidad(request.getNacionalidad().trim());
        cliente.setEstadoCivil(request.getEstadoCivil().trim());
        cliente.setCorreoElectronico(correoLimpio);
        cliente.setTelefonoMovil(request.getTelefonoMovil().trim());
        cliente.setTelefonoAlternativo(request.getTelefonoAlternativo() != null && !request.getTelefonoAlternativo().trim().isEmpty()
                ? request.getTelefonoAlternativo().trim() : null);
        cliente.setOcupacion(request.getOcupacion().trim());
        cliente.setEmpresa(request.getEmpresa().trim());
        cliente.setIngresoMensual(request.getIngresoMensual());
        cliente.setActivo(true);

        Domicilio domicilio = new Domicilio();
        domicilio.setCliente(cliente);
        domicilio.setCalle(domInput.getCalle().trim());
        domicilio.setNumeroExterior(domInput.getNumeroExterior().trim());
        domicilio.setNumeroInterior(domInput.getNumeroInterior() != null && !domInput.getNumeroInterior().trim().isEmpty()
                ? domInput.getNumeroInterior().trim() : null);
        domicilio.setColonia(domInput.getColonia().trim());
        domicilio.setMunicipio(domInput.getMunicipio().trim());
        domicilio.setEstado(domInput.getEstado().trim());
        domicilio.setCodigoPostal(domInput.getCodigoPostal().trim());
        domicilio.setPais(domInput.getPais().trim());
        cliente.setDomicilio(domicilio);

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuentaUnico());
        cuenta.setSaldo(saldoInicial);
        cuenta.setEstatus("ACTIVA");
        cuenta.setCliente(cliente);
        cliente.getCuentas().add(cuenta);

        Usuario usuario = new Usuario();
        usuario.setCliente(cliente);
        usuario.setCorreo(correoLimpio);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(true);
        cliente.setUsuario(usuario);

        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente registrado exitosamente con ID {}, cuenta {} y usuario {}",
                guardado.getId(), cuenta.getNumeroCuenta(), usuario.getCorreo());

        return mapearAClienteResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> filtrarClientes(ClienteFiltroRequest filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        if (filtro.getFechaInicio() != null && filtro.getFechaFin() != null
                && filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        Specification<Cliente> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (filtro.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filtro.getId()));
            }
            if (filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.getNombre().trim().toLowerCase() + "%"));
            }
            if (filtro.getSegundoNombre() != null && !filtro.getSegundoNombre().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("segundoNombre")), "%" + filtro.getSegundoNombre().trim().toLowerCase() + "%"));
            }
            if (filtro.getApellidoPaterno() != null && !filtro.getApellidoPaterno().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("apellidoPaterno")), "%" + filtro.getApellidoPaterno().trim().toLowerCase() + "%"));
            }
            if (filtro.getApellidoMaterno() != null && !filtro.getApellidoMaterno().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("apellidoMaterno")), "%" + filtro.getApellidoMaterno().trim().toLowerCase() + "%"));
            }
            if (filtro.getCurp() != null && !filtro.getCurp().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("curp")), filtro.getCurp().trim().toUpperCase()));
            }
            if (filtro.getRfc() != null && !filtro.getRfc().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("rfc")), filtro.getRfc().trim().toUpperCase()));
            }
            if (filtro.getCorreoElectronico() != null && !filtro.getCorreoElectronico().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("correoElectronico")), filtro.getCorreoElectronico().trim().toLowerCase()));
            }
            if (filtro.getNumeroCuenta() != null && !filtro.getNumeroCuenta().isBlank()) {
                Join<Cliente, Cuenta> cuentaJoin = root.join("cuentas", JoinType.INNER);
                predicates.add(cb.equal(cuentaJoin.get("numeroCuenta"), filtro.getNumeroCuenta().trim()));
            }
            if (filtro.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), filtro.getActivo()));
            }
            if (filtro.getFechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaInicio().atStartOfDay()));
            }
            if (filtro.getFechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaFin().atTime(LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Cliente> resultados = clienteRepository.findAll(spec);
        if (resultados.isEmpty() && (filtro.getId() != null || filtro.getCurp() != null || filtro.getRfc() != null || filtro.getNumeroCuenta() != null)) {
            throw new ClienteNoEncontradoException("No se encontró ningún cliente que coincida con los criterios de búsqueda especificados.");
        }

        return resultados.stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con ID: " + id));
        return mapearAClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCurp(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            throw new ValidacionException("Debe proporcionar una CURP para la consulta.");
        }
        Cliente cliente = clienteRepository.findByCurpIgnoreCase(curp.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con CURP: " + curp));
        return mapearAClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorRfc(String rfc) {
        if (rfc == null || rfc.trim().isEmpty()) {
            throw new ValidacionException("Debe proporcionar un RFC para la consulta.");
        }
        Cliente cliente = clienteRepository.findByRfcIgnoreCase(rfc.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con RFC: " + rfc));
        return mapearAClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new ValidacionException("Debe proporcionar un correo electrónico para la consulta.");
        }
        Cliente cliente = clienteRepository.findByCorreoElectronicoIgnoreCase(correo.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con correo electrónico: " + correo));
        return mapearAClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new ValidacionException("Debe proporcionar un número de cuenta para la consulta.");
        }
        Cliente cliente = clienteRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró ningún cliente asociado al número de cuenta: " + numeroCuenta));
        return mapearAClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre != null ? nombre.trim() : "")
                .stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorApellidoPaterno(String apellidoPaterno) {
        return clienteRepository.findByApellidoPaternoContainingIgnoreCase(apellidoPaterno != null ? apellidoPaterno.trim() : "")
                .stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorApellidoMaterno(String apellidoMaterno) {
        return clienteRepository.findByApellidoMaternoContainingIgnoreCase(apellidoMaterno != null ? apellidoMaterno.trim() : "")
                .stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarClientesActivos() {
        return clienteRepository.findByActivoTrue()
                .stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ValidacionException("Debe especificar fechaInicio y fechaFin para consultar por rango de fechas.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        return clienteRepository.findByFechaCreacionBetween(inicio, fin)
                .stream()
                .map(this::mapearAClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClienteResponse actualizarCliente(ClienteActualizacionRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidacionException("El identificador del cliente (id) es obligatorio en el cuerpo de la petición.");
        }
        return actualizarCliente(request.getId(), request);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarCliente(Long id, ClienteActualizacionRequest request) {
        if (request == null) {
            throw new ValidacionException("El cuerpo de actualización no puede ser nulo.");
        }

        if (request.getFechaCreacion() != null || request.getFechaActualizacion() != null) {
            throw new ValidacionException("No está permitido modificar campos de auditoría controlados por el servidor (fechaCreacion, fechaActualizacion).");
        }

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con ID: " + id));

        if (request.getCurp() != null && !request.getCurp().trim().equalsIgnoreCase(cliente.getCurp())) {
            throw new ValidacionException("No está permitido modificar la CURP del cliente.");
        }
        if (request.getRfc() != null && !request.getRfc().trim().equalsIgnoreCase(cliente.getRfc())) {
            throw new ValidacionException("No está permitido modificar el RFC del cliente.");
        }
        if (request.getNumeroCuenta() != null) {
            throw new ValidacionException("No está permitido modificar el número de cuenta.");
        }

        if (request.getNombre() != null) {
            ValidadorNegocio.validarNombreOApellido(request.getNombre(), "nombre", true);
            cliente.setNombre(request.getNombre().trim());
        }
        if (request.getSegundoNombre() != null) {
            if (request.getSegundoNombre().trim().isEmpty()) {
                cliente.setSegundoNombre(null);
            } else {
                ValidadorNegocio.validarNombreOApellido(request.getSegundoNombre(), "segundoNombre", false);
                cliente.setSegundoNombre(request.getSegundoNombre().trim());
            }
        }
        if (request.getApellidoPaterno() != null) {
            ValidadorNegocio.validarNombreOApellido(request.getApellidoPaterno(), "apellidoPaterno", true);
            cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        }
        if (request.getApellidoMaterno() != null) {
            ValidadorNegocio.validarNombreOApellido(request.getApellidoMaterno(), "apellidoMaterno", true);
            cliente.setApellidoMaterno(request.getApellidoMaterno().trim());
        }
        if (request.getFechaNacimiento() != null) {
            ValidadorNegocio.validarFechaNacimiento(request.getFechaNacimiento());
            cliente.setFechaNacimiento(request.getFechaNacimiento());
        }
        if (request.getSexo() != null) {
            ValidadorNegocio.validarCampoObligatorio(request.getSexo(), "sexo");
            cliente.setSexo(request.getSexo().trim());
        }
        if (request.getNacionalidad() != null) {
            ValidadorNegocio.validarCampoObligatorio(request.getNacionalidad(), "nacionalidad");
            cliente.setNacionalidad(request.getNacionalidad().trim());
        }
        if (request.getEstadoCivil() != null) {
            ValidadorNegocio.validarCampoObligatorio(request.getEstadoCivil(), "estadoCivil");
            cliente.setEstadoCivil(request.getEstadoCivil().trim());
        }

        if (request.getCorreoElectronico() != null) {
            ValidadorNegocio.validarCorreo(request.getCorreoElectronico());
            String nuevoCorreo = request.getCorreoElectronico().trim().toLowerCase();
            if (!nuevoCorreo.equalsIgnoreCase(cliente.getCorreoElectronico())) {
                Optional<Cliente> existenteCliente = clienteRepository.findByCorreoElectronicoIgnoreCase(nuevoCorreo);
                if (existenteCliente.isPresent() && !existenteCliente.get().getId().equals(cliente.getId())) {
                    throw new CorreoDuplicadoException("Ya existe otro cliente con el correo electrónico: " + nuevoCorreo);
                }
                Optional<Usuario> existenteUsuario = usuarioRepository.findByCorreoIgnoreCase(nuevoCorreo);
                if (existenteUsuario.isPresent() && (cliente.getUsuario() == null
                        || !existenteUsuario.get().getId().equals(cliente.getUsuario().getId()))) {
                    throw new CorreoDuplicadoException("Ya existe otro usuario con el correo electrónico: " + nuevoCorreo);
                }
                cliente.setCorreoElectronico(nuevoCorreo);
                if (cliente.getUsuario() != null) {
                    cliente.getUsuario().setCorreo(nuevoCorreo);
                }
            }
        }
        if (request.getTelefonoMovil() != null) {
            ValidadorNegocio.validarTelefono(request.getTelefonoMovil(), "telefonoMovil", true);
            cliente.setTelefonoMovil(request.getTelefonoMovil().trim());
        }
        if (request.getTelefonoAlternativo() != null) {
            if (request.getTelefonoAlternativo().trim().isEmpty()) {
                cliente.setTelefonoAlternativo(null);
            } else {
                ValidadorNegocio.validarTelefono(request.getTelefonoAlternativo(), "telefonoAlternativo", false);
                cliente.setTelefonoAlternativo(request.getTelefonoAlternativo().trim());
            }
        }

        Domicilio domicilio = cliente.getDomicilio();
        if (domicilio == null) {
            domicilio = new Domicilio();
            domicilio.setCliente(cliente);
            cliente.setDomicilio(domicilio);
        }
        DomicilioDto domUpdate = extraerDomicilioActualizacion(request);
        if (domUpdate.getCalle() != null) {
            ValidadorNegocio.validarCampoObligatorio(domUpdate.getCalle(), "calle");
            domicilio.setCalle(domUpdate.getCalle().trim());
        }
        if (domUpdate.getNumeroExterior() != null) {
            ValidadorNegocio.validarCampoObligatorio(domUpdate.getNumeroExterior(), "numeroExterior");
            domicilio.setNumeroExterior(domUpdate.getNumeroExterior().trim());
        }
        if (domUpdate.getNumeroInterior() != null) {
            domicilio.setNumeroInterior(domUpdate.getNumeroInterior().trim().isEmpty() ? null : domUpdate.getNumeroInterior().trim());
        }
        if (domUpdate.getColonia() != null) {
            ValidadorNegocio.validarCampoObligatorio(domUpdate.getColonia(), "colonia");
            domicilio.setColonia(domUpdate.getColonia().trim());
        }
        if (domUpdate.getMunicipio() != null) {
            ValidadorNegocio.validarCampoObligatorio(domUpdate.getMunicipio(), "municipio");
            domicilio.setMunicipio(domUpdate.getMunicipio().trim());
        }
        if (domUpdate.getEstado() != null) {
            ValidadorNegocio.validarCampoObligatorio(domUpdate.getEstado(), "estado");
            domicilio.setEstado(domUpdate.getEstado().trim());
        }
        if (domUpdate.getCodigoPostal() != null) {
            ValidadorNegocio.validarCodigoPostal(domUpdate.getCodigoPostal());
            domicilio.setCodigoPostal(domUpdate.getCodigoPostal().trim());
        }
        if (domUpdate.getPais() != null) {
            ValidadorNegocio.validarCampoObligatorio(domUpdate.getPais(), "pais");
            domicilio.setPais(domUpdate.getPais().trim());
        }

        if (request.getOcupacion() != null) {
            ValidadorNegocio.validarCampoObligatorio(request.getOcupacion(), "ocupacion");
            cliente.setOcupacion(request.getOcupacion().trim());
        }
        if (request.getEmpresa() != null) {
            ValidadorNegocio.validarCampoObligatorio(request.getEmpresa(), "empresa");
            cliente.setEmpresa(request.getEmpresa().trim());
        }
        if (request.getIngresoMensual() != null) {
            ValidadorNegocio.validarIngresoMensual(request.getIngresoMensual());
            cliente.setIngresoMensual(request.getIngresoMensual());
        }

        if (request.getActivo() != null) {
            if (!request.getActivo()) {
                aplicarBajaLogicaInterna(cliente);
            } else {
                cliente.setActivo(true);
            }
        }

        Cliente actualizado = clienteRepository.save(cliente);
        return mapearAClienteResponse(actualizado);
    }

    @Override
    @Transactional
    public ClienteResponse darBajaLogica(ClienteBajaRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidacionException("El identificador del cliente (id) es obligatorio para dar de baja.");
        }
        return darBajaLogica(request.getId());
    }

    @Override
    @Transactional
    public ClienteResponse darBajaLogica(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con ID: " + id));

        aplicarBajaLogicaInterna(cliente);
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Baja lógica aplicada al cliente con ID {} (usuario y cuentas desactivadas)", id);
        return mapearAClienteResponse(guardado);
    }

    private void aplicarBajaLogicaInterna(Cliente cliente) {
        cliente.setActivo(false);
        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setActivo(false);
        }
        if (cliente.getCuentas() != null) {
            for (Cuenta cuenta : cliente.getCuentas()) {
                if ("ACTIVA".equalsIgnoreCase(cuenta.getEstatus())) {
                    cuenta.setEstatus("INACTIVA");
                }
            }
        }
    }

    private String generarNumeroCuentaUnico() {
        String numero;
        do {
            long valor = 1_000_000_000L + (Math.abs(secureRandom.nextLong()) % 9_000_000_000L);
            numero = String.valueOf(valor);
        } while (cuentaRepository.existsByNumeroCuenta(numero));
        return numero;
    }

    private DomicilioDto extraerDomicilio(ClienteRegistroRequest req) {
        if (req.getDomicilio() != null) {
            return req.getDomicilio();
        }
        return DomicilioDto.builder()
                .calle(req.getCalle())
                .numeroExterior(req.getNumeroExterior())
                .numeroInterior(req.getNumeroInterior())
                .colonia(req.getColonia())
                .municipio(req.getMunicipio())
                .estado(req.getEstado())
                .codigoPostal(req.getCodigoPostal())
                .pais(req.getPais())
                .build();
    }

    private DomicilioDto extraerDomicilioActualizacion(ClienteActualizacionRequest req) {
        DomicilioDto dom = req.getDomicilio() != null ? req.getDomicilio() : new DomicilioDto();
        return DomicilioDto.builder()
                .calle(dom.getCalle() != null ? dom.getCalle() : req.getCalle())
                .numeroExterior(dom.getNumeroExterior() != null ? dom.getNumeroExterior() : req.getNumeroExterior())
                .numeroInterior(dom.getNumeroInterior() != null ? dom.getNumeroInterior() : req.getNumeroInterior())
                .colonia(dom.getColonia() != null ? dom.getColonia() : req.getColonia())
                .municipio(dom.getMunicipio() != null ? dom.getMunicipio() : req.getMunicipio())
                .estado(dom.getEstado() != null ? dom.getEstado() : req.getEstado())
                .codigoPostal(dom.getCodigoPostal() != null ? dom.getCodigoPostal() : req.getCodigoPostal())
                .pais(dom.getPais() != null ? dom.getPais() : req.getPais())
                .build();
    }

    private ClienteResponse mapearAClienteResponse(Cliente cliente) {
        DomicilioDto domicilioDto = null;
        if (cliente.getDomicilio() != null) {
            Domicilio d = cliente.getDomicilio();
            domicilioDto = DomicilioDto.builder()
                    .id(d.getId())
                    .calle(d.getCalle())
                    .numeroExterior(d.getNumeroExterior())
                    .numeroInterior(d.getNumeroInterior())
                    .colonia(d.getColonia())
                    .municipio(d.getMunicipio())
                    .estado(d.getEstado())
                    .codigoPostal(d.getCodigoPostal())
                    .pais(d.getPais())
                    .build();
        }

        String nombreCompleto = construirNombreCompleto(cliente);

        List<CuentaResponse> cuentasDto = cliente.getCuentas() == null ? Collections.emptyList() :
                cliente.getCuentas().stream()
                        .map(c -> CuentaResponse.builder()
                                .id(c.getId())
                                .numeroCuenta(c.getNumeroCuenta())
                                .saldo(c.getSaldo())
                                .estatus(c.getEstatus())
                                .clienteId(cliente.getId())
                                .nombreCliente(nombreCompleto)
                                .fechaCreacion(c.getFechaCreacion())
                                .fechaActualizacion(c.getFechaActualizacion())
                                .build())
                        .collect(Collectors.toList());

        UsuarioResponse usuarioDto = null;
        if (cliente.getUsuario() != null) {
            Usuario u = cliente.getUsuario();
            usuarioDto = UsuarioResponse.builder()
                    .id(u.getId())
                    .clienteId(cliente.getId())
                    .correo(u.getCorreo())
                    .activo(u.getActivo())
                    .fechaCreacion(u.getFechaCreacion())
                    .fechaActualizacion(u.getFechaActualizacion())
                    .build();
        }

        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .segundoNombre(cliente.getSegundoNombre())
                .apellidoPaterno(cliente.getApellidoPaterno())
                .apellidoMaterno(cliente.getApellidoMaterno())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .curp(cliente.getCurp())
                .rfc(cliente.getRfc())
                .sexo(cliente.getSexo())
                .nacionalidad(cliente.getNacionalidad())
                .estadoCivil(cliente.getEstadoCivil())
                .correoElectronico(cliente.getCorreoElectronico())
                .telefonoMovil(cliente.getTelefonoMovil())
                .telefonoAlternativo(cliente.getTelefonoAlternativo())
                .ocupacion(cliente.getOcupacion())
                .empresa(cliente.getEmpresa())
                .ingresoMensual(cliente.getIngresoMensual())
                .activo(cliente.getActivo())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .domicilio(domicilioDto)
                .cuentas(cuentasDto)
                .usuario(usuarioDto)
                .build();
    }

    private String construirNombreCompleto(Cliente c) {
        StringBuilder sb = new StringBuilder(c.getNombre());
        if (c.getSegundoNombre() != null && !c.getSegundoNombre().isBlank()) {
            sb.append(" ").append(c.getSegundoNombre());
        }
        sb.append(" ").append(c.getApellidoPaterno()).append(" ").append(c.getApellidoMaterno());
        return sb.toString();
    }
}
