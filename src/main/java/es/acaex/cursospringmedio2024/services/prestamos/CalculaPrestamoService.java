package es.acaex.cursospringmedio2024.services.prestamos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import es.acaex.cursospringmedio2024.exceptions.PrestamoNoGestionableException;
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

    private final static Map<String, Map<String, Integer>> diasPorPerfilSituacion = new HashMap<String, Map<String, Integer>>() {
        {
            put("catedratico", new HashMap<String, Integer>() {
                {
                    put("vacaciones", 90);
                    put("horariohabitual", 90);
                    put("horarioguardia", 90);
                }
            });
            put("profesor", new HashMap<String, Integer>() {
                {
                    put("vacaciones", 60);
                    put("horariohabitual", 30);
                    put("horarioguardia", 15);
                }
            });
            put("estudiante", new HashMap<String, Integer>() {
                {
                    put("vacaciones", 0);
                    put("horariohabitual", 15);
                    put("horarioguardia", 7);
                }
            });
            put("visitante", new HashMap<String, Integer>() {
                {
                    put("vacaciones", 0);
                    put("horariohabitual", 7);
                    put("horarioguardia", 3);
                }
            });
        }
    };

    public Prestamo execute(Socio socio, Libro libro, LocalDate localDate, LocalTime localTime) {

        validarQuePrestamoEsViable(socio, libro, localDate);

        int diasPrestamo = getDiasPrestamo(socio, localDate, localTime);

        diasPrestamo = modificacionesPorSituacionesAdicionales(diasPrestamo, libro);

        return Prestamo.builder()
                .libro(libro)
                .socio(socio)
                .expiraEn(localDate.plusDays(diasPrestamo))
                .build();

    }

    private int modificacionesPorSituacionesAdicionales(int diasPrestamo, Libro libro) {
        if (libro.esMuyDemandado()) {
            diasPrestamo = diasPrestamo - 3;
        }
        return diasPrestamo;
    }

    private int getDiasPrestamo(Socio socio, LocalDate localDate, LocalTime localTime) {
        String perfil = socio.getPerfil();
        String situacion = getSituacion(localDate, localTime);

        int diasPrestamo = diasPorPerfilSituacion.get(perfil).get(situacion);
        if (diasPrestamo == 0) {
            throw new PrestamoNoGestionableException(
                    "No se puede gestionar ese perfil/situacion (Fin de Semana/Verano)");
        }
        return diasPrestamo;
    }

    private String getSituacion(LocalDate localDate, LocalTime localTime) {
        if (isSummer(localDate)) {
            return "vacaciones";
        }
        if (!isHorasLectivas(localTime)) {
            return "horarioguardia";
        }
        return "horariohabitual";
    }

    private void validarQuePrestamoEsViable(Socio socio, Libro libro, LocalDate localDate) {
        if (socio.tienePrestamoVencido()) {
            throw new PrestamoNoGestionableException("El Socio tiene un préstamo retrasado");
        }
        if (socio.haSuperadoElLimiteDePrestamo()) {
            throw new PrestamoNoGestionableException("El Socio ha superado el límite de libros prestados");
        }
        if (libro.estaEnPrestamo()) {
            throw new PrestamoNoGestionableException("El Libro está prestado");
        }
        if (!socio.getPerfil().equals("profesor") && isWeekend(localDate)) {
            throw new PrestamoNoGestionableException("Fin de Semana");
        }
    }

    private boolean isWeekend(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() > 5;
    }

    private boolean isSummer(LocalDate localDate) {
        return localDate.getMonth().equals(Month.JULY)
                || localDate.getMonth().equals(Month.AUGUST);
    }

    private boolean isHorasLectivas(LocalTime localTime) {
        return localTime.isAfter(LocalTime.of(7, 59))
                && localTime.isBefore(LocalTime.of(22, 1));
    }

}
