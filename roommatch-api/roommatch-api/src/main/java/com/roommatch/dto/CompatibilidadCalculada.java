package com.roommatch.dto;

import java.math.BigDecimal;


public class CompatibilidadCalculada {

    private BigDecimal porcentaje;

    private String coincidencias;

    private String diferencias;


    public CompatibilidadCalculada() {
    }


    public CompatibilidadCalculada(

            BigDecimal porcentaje,

            String coincidencias,

            String diferencias

    ) {

        this.porcentaje = porcentaje;

        this.coincidencias = coincidencias;

        this.diferencias = diferencias;

    }


    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(
            BigDecimal porcentaje
    ) {
        this.porcentaje = porcentaje;
    }


    public String getCoincidencias() {
        return coincidencias;
    }

    public void setCoincidencias(
            String coincidencias
    ) {
        this.coincidencias = coincidencias;
    }


    public String getDiferencias() {
        return diferencias;
    }

    public void setDiferencias(
            String diferencias
    ) {
        this.diferencias = diferencias;
    }

}