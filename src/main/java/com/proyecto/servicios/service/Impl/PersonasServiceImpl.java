package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.CuentaBancaria;
import com.proyecto.servicios.entity.sf.Personas;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.repositorys.sf.PersonasRepository;
import com.proyecto.servicios.service.PersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class PersonasServiceImpl implements PersonaService {

    @Autowired
    private PersonasRepository personasRepository;

    @Override
    public PersonaResponse creaPersona(PersonasRequest personasRequest) {
        PersonaResponse person = new PersonaResponse();
        Personas persona = new Personas();

        BeanUtils.copyProperties(personasRequest, persona);

        CuentaBancaria cuenta = new CuentaBancaria();
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setPersona(persona);

        persona.setCuentaBancaria(cuenta);

        personasRepository.save(persona);

        person.setCodigo(0);
        person.setMensaje("Éxito. Cliente registrado con la cuenta: " + cuenta.getNumeroCuenta());
        BeanUtils.copyProperties(persona, person);

        return person;
    }

    @Override
    public GenericResponse eliminaPersona(EliminaPersonaRequest eliminaPersonaRequest) {
        GenericResponse genericResponse = new GenericResponse();

        Optional<Personas> existePersona = personasRepository.findByNombre(eliminaPersonaRequest.getNombre());
        if (existePersona.isPresent()) {
            Personas personaElimina = existePersona.get();
            personasRepository.delete(personaElimina);
            genericResponse.setCodigo(0);
            genericResponse.setMensaje("El cliente y su cuenta han sido eliminados correctamente");
        } else {
            genericResponse.setCodigo(1);
            genericResponse.setMensaje("El cliente no existe");
        }
        return genericResponse;
    }

    @Override
    public GenericResponse actualizaPersona(PersonasRequest personasRequest) {
        GenericResponse genericResponse = new GenericResponse();
        Optional<Personas> existePersona = personasRepository.findByNombre(personasRequest.getNombre());

        if (existePersona.isPresent()) {
            Personas personaActualiza = existePersona.get();

            BeanUtils.copyProperties(personasRequest, personaActualiza, "id", "cuentaBancaria");

            personasRepository.save(personaActualiza);
            genericResponse.setCodigo(0);
            genericResponse.setMensaje("El cliente ha sido actualizado correctamente");

        } else {
            genericResponse.setCodigo(1);
            genericResponse.setMensaje("El cliente no existe");
        }
        return genericResponse;
    }

    private String generarNumeroCuenta() {
        Random random = new Random();
        long numero = (long) (random.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(numero);
    }
}
