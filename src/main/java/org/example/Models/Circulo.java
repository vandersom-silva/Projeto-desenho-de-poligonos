package org.example.Models;

import java.util.ArrayList;
import java.util.List;

public class Circulo extends FormaBase {

    private Ponto centro;
    private double raio;

    public Circulo(Ponto centro, double raio) {

        if (centro == null) {
            throw new IllegalArgumentException(
                    "O centro não pode ser nulo."
            );
        }

        if (raio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser maior que zero."
            );
        }

        this.centro = centro.clonar();
        this.raio = raio;

        /*
         * Mantemos dois pontos na lista:
         * ponto 0 = centro
         * ponto 1 = ponto da borda
         */
        super.pontos.add(this.centro.clonar());

        super.pontos.add(
                new Ponto(
                        centro.getX() + raio,
                        centro.getY()
                )
        );
    }

    public Ponto getCentro() {
        return centro;
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {

        if (raio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser maior que zero."
            );
        }

        this.raio = raio;

        atualizarPontoDaBorda();
    }

    @Override
    public void adicionarPonto(Ponto p) {

        if (p == null) {
            return;
        }

        /*
         * Primeiro ponto define o centro.
         * Segundo ponto define a borda/raio.
         */
        if (pontos.isEmpty()) {

            centro = p.clonar();
            pontos.add(centro.clonar());

        } else if (pontos.size() == 1) {

            double dx =
                    p.getX() - centro.getX();

            double dy =
                    p.getY() - centro.getY();

            double novoRaio =
                    Math.sqrt(dx * dx + dy * dy);

            if (novoRaio > 0) {

                raio = novoRaio;

                pontos.add(p.clonar());
            }
        }
    }

    private void atualizarPontoDaBorda() {

        if (pontos.size() >= 2) {

            pontos.get(0).setX(
                    centro.getX()
            );

            pontos.get(0).setY(
                    centro.getY()
            );

            pontos.get(1).setX(
                    centro.getX() + raio
            );

            pontos.get(1).setY(
                    centro.getY()
            );
        }
    }

    @Override
    public List<Ponto> getPontos() {
        return pontos;
    }

    @Override
    public int getQuantidadePontos() {
        return pontos.size();
    }

    @Override
    public double calcularArea() {
        return Math.PI * raio * raio;
    }

    @Override
    public double calcularPerimetro() {
        return 2 * Math.PI * raio;
    }

    @Override
    public Ponto getCentroide() {
        return centro.clonar();
    }

    @Override
    public boolean contemPonto(
            double px,
            double py) {

        Ponto ponto =
                new Ponto(px, py);

        return centro.distancia(ponto)
                <= raio;
    }

    @Override
    public void transladar(
            double dx,
            double dy) {

        centro.mover(dx, dy);

        atualizarPontoDaBorda();
    }

    @Override
    public void escalar(double fator) {

        if (fator <= 0) {
            throw new IllegalArgumentException(
                    "O fator de escala deve ser maior que zero."
            );
        }

        raio *= fator;

        atualizarPontoDaBorda();
    }

    @Override
    public void rotacionar(
            double anguloRadianos) {

        /*
         * A rotação não altera visualmente um círculo.
         * Rotacionamos apenas o ponto da borda
         * usado internamente para representar o raio.
         */

        double dx = raio * Math.cos(anguloRadianos);
        double dy = raio * Math.sin(anguloRadianos);

        pontos.get(1).setX(
                centro.getX() + dx
        );

        pontos.get(1).setY(
                centro.getY() + dy
        );
    }

    @Override
    public void cisalhar(
            double shx,
            double shy) {

        /*
         * Cisalhamento transforma matematicamente
         * um círculo em uma elipse.
         *
         * Como este modelo ainda representa apenas
         * círculos, deixaremos a operação sem efeito
         * nesta primeira etapa.
         */
    }

    @Override
    public FormaGeometrica clonar() {

        Circulo copia =
                new Circulo(
                        centro,
                        raio
                );

        copia.setCorPreenchimentoHex(
                this.corPreenchimentoHex
        );

        copia.setCorBordaHex(
                this.corBordaHex
        );

        copia.setEspessuraBorda(
                this.espessuraBorda
        );

        copia.setGroupId(
                this.groupId
        );

        return copia;
    }
}