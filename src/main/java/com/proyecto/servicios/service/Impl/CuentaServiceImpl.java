package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.ValidacionException;
import com.proyecto.servicios.model.CuentaActualizacionRequest;
import com.proyecto.servicios.model.CuentaRequest;
import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.model.SaldoCuentaResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.CuentaService;
import com.proyecto.servicios.util.ValidadorNegocio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CuentaServiceImpl implements CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Value("${onboarding.cuenta.saldo-inicial:1000.00}")
    private BigDecimal saldoInicialSistema;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public CuentaResponse consultarPorNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new ValidacionException("El número de cuenta es obligatorio.");
        }
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta con número: " + numeroCuenta));
        return mapearACuentaResponse(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorClienteId(Long clienteId) {
        if (clienteId == null) {
            throw new ValidacionException("El identificador del cliente es obligatorio.");
        }
        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNoEncontradoException("No se encontró el cliente con ID: " + clienteId);
        }
        return cuentaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapearACuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorEstatus(String estatus) {
        if (estatus == null || estatus.trim().isEmpty()) {
            throw new ValidacionException("El estatus de búsqueda es obligatorio.");
        }
        return cuentaRepository.findByEstatusIgnoreCase(estatus.trim())
                .stream()
                .map(this::mapearACuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarCuentasActivas() {
        return cuentaRepository.findByEstatusIgnoreCase("ACTIVA")
                .stream()
                .map(this::mapearACuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarTodas() {
        return cuentaRepository.findAll()
                .stream()
                .map(this::mapearACuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaldoCuentaResponse consultarSaldo(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new ValidacionException("El número de cuenta es obligatorio.");
        }
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta con número: " + numeroCuenta));
        return SaldoCuentaResponse.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .saldo(cuenta.getSaldo())
                .estatus(cuenta.getEstatus())
                .clienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : null)
                .build();
    }

    @Override
    @Transactional
    public CuentaResponse crearCuenta(CuentaRequest request) {
        if (request == null || request.getClienteId() == null) {
            throw new ValidacionException("El clienteId es obligatorio para crear una cuenta bancaria.");
        }

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con ID: " + request.getClienteId()));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ValidacionException("No se puede crear una cuenta activa porque el cliente con ID " + cliente.getId() + " se encuentra inactivo.");
        }

        BigDecimal saldo = request.getSaldo() != null ? request.getSaldo() : saldoInicialSistema;
        ValidadorNegocio.validarSaldoCuenta(saldo);

        String numeroCuenta;
        if (request.getNumeroCuenta() != null && !request.getNumeroCuenta().trim().isEmpty()) {
            numeroCuenta = request.getNumeroCuenta().trim();
            if (cuentaRepository.existsByNumeroCuenta(numeroCuenta)) {
                throw new ValidacionException("El número de cuenta '" + numeroCuenta + "' ya existe.");
            }
        } else {
            numeroCuenta = generarNumeroCuentaUnico();
        }

        String estatus = (request.getEstatus() != null && !request.getEstatus().trim().isEmpty())
                ? request.getEstatus().trim().toUpperCase()
                : "ACTIVA";

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setSaldo(saldo);
        cuenta.setEstatus(estatus);
        cuenta.setCliente(cliente);

        Cuenta guardada = cuentaRepository.save(cuenta);
        return mapearACuentaResponse(guardada);
    }

    @Override
    @Transactional
    public CuentaResponse actualizarCuenta(String numeroCuenta, CuentaActualizacionRequest request) {
        if (request == null) {
            throw new ValidacionException("El cuerpo de actualización de la cuenta no puede ser nulo.");
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta con número: " + numeroCuenta));

        if (request.getNumeroCuenta() != null && !request.getNumeroCuenta().trim().equals(cuenta.getNumeroCuenta())) {
            throw new ValidacionException("No está permitido modificar el número de cuenta.");
        }

        if (request.getSaldo() != null) {
            ValidadorNegocio.validarSaldoCuenta(request.getSaldo());
            cuenta.setSaldo(request.getSaldo());
        }

        if (request.getEstatus() != null && !request.getEstatus().trim().isEmpty()) {
            String nuevoEstatus = request.getEstatus().trim().toUpperCase();
            if ("ACTIVA".equalsIgnoreCase(nuevoEstatus)) {
                Cliente cliente = cuenta.getCliente();
                if (cliente != null && !Boolean.TRUE.equals(cliente.getActivo())) {
                    throw new ValidacionException("Solo los clientes activos pueden tener cuentas activas. El cliente asociado se encuentra inactivo.");
                }
            }
            cuenta.setEstatus(nuevoEstatus);
        }

        Cuenta actualizada = cuentaRepository.save(cuenta);
        return mapearACuentaResponse(actualizada);
    }

    private String generarNumeroCuentaUnico() {
        String numero;
        do {
            long valor = 1_000_000_000L + (Math.abs(secureRandom.nextLong()) % 9_000_000_000L);
            numero = String.valueOf(valor);
        } while (cuentaRepository.existsByNumeroCuenta(numero));
        return numero;
    }

    private CuentaResponse mapearACuentaResponse(Cuenta cuenta) {
        Cliente c = cuenta.getCliente();
        String nombreCompleto = null;
        Long clienteId = null;
        if (c != null) {
            clienteId = c.getId();
            StringBuilder sb = new StringBuilder(c.getNombre());
            if (c.getSegundoNombre() != null && !c.getSegundoNombre().isBlank()) {
                sb.append(" ").append(c.getSegundoNombre());
            }
            sb.append(" ").append(c.getApellidoPaterno()).append(" ").append(c.getApellidoMaterno());
            nombreCompleto = sb.toString();
        }
        return CuentaResponse.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .saldo(cuenta.getSaldo())
                .estatus(cuenta.getEstatus())
                .clienteId(clienteId)
                .nombreCliente(nombreCompleto)
                .fechaCreacion(cuenta.getFechaCreacion())
                .fechaActualizacion(cuenta.getFechaActualizacion())
                .build();
    }
}
