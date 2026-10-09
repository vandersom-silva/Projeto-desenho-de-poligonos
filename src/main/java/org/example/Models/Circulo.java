package org.example.Models;

public class Circulo extends FormaBase {

    private Ponto centro;
    private double raio;
    private boolean cisalhado;

    private static final int QUANTIDADE_PONTOS = 72;

    public Circulo(Ponto centro, double raio) {

        if (centro == null) {
            throw new IllegalArgumentException(
                    "O centro não pode ser nulo."
            );
        }

        if (!Double.isFinite(raio) || raio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser positivo e finito."
            );
        }

        this.centro = centro.clonar();
        this.raio = raio;
        this.cisalhado = false;

        gerarCircunferencia();
    }

    public Ponto getCentro() {
        return centro.clonar();
    }

    public double getRaio() {
        return raio;
    }

    public boolean isCisalhado() {
        return cisalhado;
    }

    public void setRaio(double novoRaio) {

        if (!Double.isFinite(novoRaio) || novoRaio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser positivo e finito."
            );
        }

        double fator = novoRaio / raio;

        // Redimensiona os pontos existentes, preservando
        // cisalhamento e orientação anteriores.
        for (Ponto ponto : pontos) {

            double dx = ponto.getX() - centro.getX();
            double dy = ponto.getY() - centro.getY();

            ponto.setX(
                    centro.getX() + dx * fator
            );

            ponto.setY(
                    centro.getY() + dy * fator
            );
        }

        raio = novoRaio;
    }

    private void gerarCircunferencia() {

        pontos.clear();

        for (int i = 0; i < QUANTIDADE_PONTOS; i++) {

            double angulo = 2 * Math.PI
                    * i / QUANTIDADE_PONTOS;

            double x = centro.getX()
                    + raio * Math.cos(angulo);

            double y = centro.getY()
                    + raio * Math.sin(angulo);

            pontos.add(new Ponto(x, y));
        }
    }

    @Override
    public void adicionarPonto(Ponto p) {
        // O círculo é definido pelo centro e pelo raio.
        // Seus pontos de contorno são calculados automaticamente.
    }

    @Override
    public int getQuantidadePontos() {
        return pontos.size();
    }

    @Override
    public double calcularArea() {

        if (!cisalhado) {
            return Math.PI * raio * raio;
        }

        // Após o cisalhamento, calcula a área aproximada
        // do contorno transformado pela fórmula de Gauss.
        double soma = 0;

        for (int i = 0; i < pontos.size(); i++) {

            Ponto atual = pontos.get(i);
            Ponto proximo =
                    pontos.get((i + 1) % pontos.size());

            soma += atual.getX() * proximo.getY()
                    - proximo.getX() * atual.getY();
        }

        return Math.abs(soma) / 2.0;
    }

    @Override
    public double calcularPerimetro() {

        if (!cisalhado) {
            return 2 * Math.PI * raio;
        }

        // Aproxima o perímetro somando os segmentos
        // que formam o contorno transformado.
        double perimetro = 0;

        for (int i = 0; i < pontos.size(); i++) {

            Ponto atual = pontos.get(i);
            Ponto proximo =
                    pontos.get((i + 1) % pontos.size());

            perimetro += atual.distancia(proximo);
        }

        return perimetro;
    }

    @Override
    public Ponto getCentroide() {
        return centro.clonar();
    }

    @Override
    public boolean contemPonto(double px, double py) {

        if (!cisalhado) {

            return centro.distancia(
                    new Ponto(px, py)
            ) <= raio;
        }

        // Depois do cisalhamento, verifica se o ponto
        // está dentro do polígono que aproxima o círculo.
        boolean dentro = false;

        for (int i = 0, j = pontos.size() - 1;
             i < pontos.size();
             j = i++) {

            double xi = pontos.get(i).getX();
            double yi = pontos.get(i).getY();

            double xj = pontos.get(j).getX();
            double yj = pontos.get(j).getY();

            boolean intersecao =
                    ((yi > py) != (yj > py))
                            && (px < (xj - xi) * (py - yi)
                            / (yj - yi) + xi);

            if (intersecao) {
                dentro = !dentro;
            }
        }

        return dentro;
    }

    @Override
    public void transladar(double dx, double dy) {

        centro.mover(dx, dy);

        for (Ponto ponto : pontos) {
            ponto.mover(dx, dy);
        }
    }

    @Override
    public void escalar(double fator) {

        if (!Double.isFinite(fator) || fator <= 0) {
            throw new IllegalArgumentException(
                    "O fator de escala deve ser positivo e finito."
            );
        }

        for (Ponto ponto : pontos) {

            double dx = ponto.getX() - centro.getX();
            double dy = ponto.getY() - centro.getY();

            ponto.setX(
                    centro.getX() + dx * fator
            );

            ponto.setY(
                    centro.getY() + dy * fator
            );
        }

        raio *= fator;
    }

    @Override
    public void rotacionar(double anguloRadianos) {

        if (!Double.isFinite(anguloRadianos)) {
            throw new IllegalArgumentException(
                    "O ângulo deve ser finito."
            );
        }

        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);

        for (Ponto ponto : pontos) {

            double dx = ponto.getX() - centro.getX();
            double dy = ponto.getY() - centro.getY();

            double novoX = centro.getX()
                    + dx * cos - dy * sin;

            double novoY = centro.getY()
                    + dx * sin + dy * cos;

            ponto.setX(novoX);
            ponto.setY(novoY);
        }
    }

    @Override
    public void cisalhar(double shx, double shy) {

        if (!Double.isFinite(shx) || !Double.isFinite(shy)) {
            throw new IllegalArgumentException(
                    "Os fatores de cisalhamento devem ser finitos."
            );
        }

        // Transformamos todos os pontos usando as
        // coordenadas anteriores à transformação.
        for (Ponto ponto : pontos) {

            double x = ponto.getX();
            double y = ponto.getY();

            double novoX = x + shx * y;
            double novoY = y + shy * x;

            ponto.setX(novoX);
            ponto.setY(novoY);
        }

        // Aplicamos a mesma transformação ao centro.
        double centroX = centro.getX();
        double centroY = centro.getY();

        centro.setX(
                centroX + shx * centroY
        );

        centro.setY(
                centroY + shy * centroX
        );

        if (shx != 0 || shy != 0) {
            cisalhado = true;
        }
    }

    @Override
    public FormaGeometrica clonar() {

        Circulo copia = new Circulo(centro, raio);

        // Preserva exatamente o contorno atual, inclusive
        // quando já houve cisalhamento ou rotação.
        for (int i = 0; i < pontos.size(); i++) {

            Ponto original = pontos.get(i);
            Ponto destino = copia.pontos.get(i);

            destino.setX(original.getX());
            destino.setY(original.getY());
        }

        copia.cisalhado = this.cisalhado;

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