package com.roommatch.dto;

import java.math.BigDecimal;


public class CompatibilidadCalculada {

    private BigDecimal porcentaje;
    private int cobertura;
    private String versionAlgoritmo;
    private Boolean hayImporteComun;
    private java.util.List<com.roommatch.matching.CompatibilityCalculator.Criterion> criterios = java.util.List.of();
    public int getCobertura() { return cobertura; }
    public String getVersionAlgoritmo() { return versionAlgoritmo; }
    public Boolean getHayImporteComun() { return hayImporteComun; }
    public java.util.List<com.roommatch.matching.CompatibilityCalculator.Criterion> getCriterios() { return criterios; }
    public CompatibilidadCalculada(com.roommatch.matching.CompatibilityCalculator.Result result) {
        this(result.porcentaje(), result.coincidencias(), result.diferencias());
        cobertura = result.cobertura();
        versionAlgoritmo = com.roommatch.matching.CompatibilityCalculator.VERSION;
        hayImporteComun = result.hayImporteComun();
        criterios = result.criterios();
    }


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