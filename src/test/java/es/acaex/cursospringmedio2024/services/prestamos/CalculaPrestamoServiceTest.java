package es.acaex.cursospringmedio2024.services.prestamos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.acaex.cursospringmedio2024.exceptions.PrestamoNoGestionableException;
import es.acaex.cursospringmedio2024.models.Libro;
import es.acaex.cursospringmedio2024.models.Prestamo;
import es.acaex.cursospringmedio2024.models.Socio;

public class CalculaPrestamoServiceTest {

    CalculaPrestamoService calculaPrestamoService;

    @Mock
    Socio socio;
    @Mock
    Libro libro;

    LocalDate fechaDePrestamo;
    LocalTime horaPrestamo = LocalTime.parse("10:00");

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        calculaPrestamoService = new CalculaPrestamoService();
        when(socio.getPerfil()).thenReturn("visitante");
    }

    @Test
    void unVisitanteTiene7DiasDePrestamo() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro, fechaDePrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-09-09"));
    }

    @Test
    void unEstudianteTiene15DiasDePrestamo() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        when(socio.getPerfil()).thenReturn("estudiante");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro, fechaDePrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-09-17"));
    }

    @Test
    void unProfesorTiene30DiasDePrestamo() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        when(socio.getPerfil()).thenReturn("profesor");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro, fechaDePrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-10-02"));
    }

    @Test
    void noSePuedePrestarSiElLibroYaEstaEnUnPrestamoActivo() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        when(libro.estaEnPrestamo()).thenReturn(true);

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("prestado"));
    }

    @Test
    void noSePuedePrestarSiElSocioHaSuperadoElLimiteDePrestamos() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        when(socio.haSuperadoElLimiteDePrestamo()).thenReturn(true);

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("prestados"));
    }

    @Test
    void noSePuedePrestarSiElSocioTieneUnPrestamoRetrasado() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        when(socio.tienePrestamoVencido()).thenReturn(true);

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("retrasado"));
    }

    @Test
    void unVisitanteNoPuedeRealizarUnPrestamoEnDomingo() {
        fechaDePrestamo = LocalDate.parse("2024-09-01");
        when(socio.getPerfil()).thenReturn("visitante");

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("Fin de Semana"));
    }

    @Test
    void unEstudianteNoPuedeRealizarUnPrestamoEnSabado() {
        fechaDePrestamo = LocalDate.parse("2024-08-31");
        when(socio.getPerfil()).thenReturn("estudiante");

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("Fin de Semana"));
    }

    @Test
    void unProfesorPuedeRealizarUnPrestamoEnDomingo() {
        fechaDePrestamo = LocalDate.parse("2024-09-01");
        when(socio.getPerfil()).thenReturn("profesor");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro,
                fechaDePrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-10-01"));
    }

    @Test
    void unProfesorEnHorarioNoHabitualPrestamode15Dias() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        horaPrestamo = LocalTime.of(7, 59);
        when(socio.getPerfil()).thenReturn("profesor");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro,
                fechaDePrestamo, horaPrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-09-17"));
    }

    @Test
    void unEstudianteEnHorarioNoHabitualPrestamode7Dias() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        horaPrestamo = LocalTime.of(7, 59);
        when(socio.getPerfil()).thenReturn("estudiante");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro,
                fechaDePrestamo, horaPrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-09-09"));
    }

    @Test
    void unVisitanteEnHorarioNoHabitualPrestamode3Dias() {
        fechaDePrestamo = LocalDate.parse("2024-09-02");
        horaPrestamo = LocalTime.of(7, 59);
        when(socio.getPerfil()).thenReturn("visitante");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro,
                fechaDePrestamo, horaPrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-09-05"));
    }

    @Test
    void unProfesorEnVacacionesPuedeSacarPrestamoPor60Dias() {
        fechaDePrestamo = LocalDate.parse("2024-08-15");
        when(socio.getPerfil()).thenReturn("profesor");

        Prestamo prestamo = calculaPrestamoService.execute(socio, libro,
                fechaDePrestamo, horaPrestamo);

        assertThat(prestamo.getExpiraEn().toString(), is("2024-10-14"));
    }

    @Test
    void unEstudianteNoPuedeRealizarUnPrestamoEnVacaciones() {
        fechaDePrestamo = LocalDate.parse("2024-08-15");
        when(socio.getPerfil()).thenReturn("estudiante");

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("Verano"));
    }

    @Test
    void unVisitanteNoPuedeRealizarUnPrestamoEnVacaciones() {
        fechaDePrestamo = LocalDate.parse("2024-08-15");
        when(socio.getPerfil()).thenReturn("visitante");

        Exception ex = assertThrows(PrestamoNoGestionableException.class, () -> {
            calculaPrestamoService.execute(socio, libro, fechaDePrestamo);
        });

        assertThat(ex.getMessage(), containsString("Verano"));
    }
}
