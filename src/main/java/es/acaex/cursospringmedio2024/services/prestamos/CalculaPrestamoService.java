package es.acaex.cursospringmedio2024.services.prestamos;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import es.acaex.cursospringmedio2024.models.Libro;
import es.acaex.cursospringmedio2024.models.Prestamo;
import es.acaex.cursospringmedio2024.models.Socio;

@Service
public class CalculaPrestamoService {

    /**
     * Excepciones que se pueden lanzar
     * 
     * throw new PrestamoNoGestionableException("El Socio tiene un préstamo
     * retrasado");
     * throw new PrestamoNoGestionableException("El Libro está prestado");
     * throw new PrestamoNoGestionableException("El Socio ha superado el límite de
     * libros prestados");
     * 
     */

    public Prestamo execute(Socio socio, Libro libro) {
        return execute(socio, libro, LocalDate.now());
    }

    public Prestamo execute(Socio socio, Libro libro, LocalDate localDate) {
        return execute(socio, libro, localDate, LocalTime.now());
    }

    public Prestamo execute(Socio socio, Libro libro, LocalDate localDate, LocalTime localTime) {
        return Prestamo.builder()
                .libro(libro)
                .socio(socio)
                .expiraEn(localDate.plusDays(7))
                .build();
    }

}
