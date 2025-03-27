package es.acaex.cursospringmedio2024.services.prestamos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

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

    public Prestamo execute(Socio socio, Libro libro, LocalDate localDate, LocalTime localTime) {

        if (!socio.tienePrestamoVencido()) {
            if (!socio.haSuperadoElLimiteDePrestamo()) {
                if (!libro.estaEnPrestamo()) {
                    int diasPrestamo = 0;
                    switch (socio.getPerfil()) {
                        case "visitante":
                            if (isWeekend(localDate) || isSummer(localDate)) {
                                throw new PrestamoNoGestionableException("No puede sacar en Fin de Semana o Verano");
                            }
                            if (isHorasLectivas(localTime)) {
                                diasPrestamo = 7;
                            } else {
                                diasPrestamo = 3;
                            }
                            break;
                        case "estudiante":
                            if (isWeekend(localDate) || isSummer(localDate)) {
                                throw new PrestamoNoGestionableException("No puede sacar en Fin de Semana o Verano");
                            }
                            if (isHorasLectivas(localTime)) {
                                diasPrestamo = 15;
                            } else {
                                diasPrestamo = 7;
                            }
                            break;
                        case "profesor":
                            if (isSummer(localDate)) {
                                diasPrestamo = 60;
                            } else if (isHorasLectivas(localTime)) {
                                diasPrestamo = 30;
                            } else {
                                diasPrestamo = 15;
                            }
                            break;
                        default:
                            break;
                    }

                    return Prestamo.builder()
                            .libro(libro)
                            .socio(socio)
                            .expiraEn(localDate.plusDays(diasPrestamo))
                            .build();
                } else {
                    throw new PrestamoNoGestionableException("El Libro está prestado");
                }
            } else {
                throw new PrestamoNoGestionableException("El Socio ha superado el límite de libros prestados");
            }
        } else {
            throw new PrestamoNoGestionableException("El Socio tiene un préstamo retrasado");
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
