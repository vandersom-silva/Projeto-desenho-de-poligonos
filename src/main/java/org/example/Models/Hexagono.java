package org.example.Models;

public class Hexagono extends FormaBase {

    private Ponto centro;
    private double raio;

    public Hexagono(Ponto centro, double raio) {

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

        calcularVertices();
    }

    public Ponto getCentro() {
        return centro.clonar();
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double novoRaio) {

        if (!Double.isFinite(novoRaio) || novoRaio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser positivo e finito."
            );
        }

        if (raio <= 0) {
            raio = novoRaio;
            calcularVertices();
            return;
        }

        // Redimensiona a geometria existente, sem
        // apagar a orientação ou o cisalhamento.
        double fator = novoRaio / raio;

        for (Ponto ponto : pontos) {

            double dx = ponto.getX() - centro.getX();
            double dy = ponto.getY() - centro.getY();

            ponto.setX(centro.getX() + dx * fator);
            ponto.setY(centro.getY() + dy * fator);
        }

        raio = novoRaio;
    }

    private void calcularVertices() {

        pontos.clear();

        // Seis vértices separados por 60 graus.
        for (int i = 0; i < 6; i++) {

            double angulo = Math.toRadians(i * 60);

            double x = centro.getX()
                    + raio * Math.cos(angulo);

            double y = centro.getY()
                    + raio * Math.sin(angulo);

            pontos.add(new Ponto(x, y));
        }
    }

    @Override
    public void adicionarPonto(Ponto p) {
        // Os vértices são calculados automaticamente.
    }

    @Override
    public double calcularArea() {

        if (pontos.size() < 3) {
            return 0;
        }

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

        if (pontos.size() < 2) {
            return 0;
        }

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

        // Move os vértices existentes para preservar
        // as transformações realizadas anteriormente.
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

        // Escala em torno do centro atual.
        for (Ponto ponto : pontos) {

            double dx = ponto.getX() - centro.getX();
            double dy = ponto.getY() - centro.getY();

            ponto.setX(centro.getX() + dx * fator);
            ponto.setY(centro.getY() + dy * fator);
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

        // Transforma os vértices atuais diretamente.
        for (Ponto ponto : pontos) {

            double x = ponto.getX();
            double y = ponto.getY();

            ponto.setX(x + shx * y);
            ponto.setY(y + shy * x);
        }

        // Aplica a mesma transformação ao centro.
        double centroX = centro.getX();
        double centroY = centro.getY();

        centro.setX(centroX + shx * centroY);
        centro.setY(centroY + shy * centroX);

        atualizarRaioMedio();
    }

    private void atualizarRaioMedio() {

        if (pontos.isEmpty()) {
            return;
        }

        double soma = 0;

        for (Ponto ponto : pontos) {
            soma += centro.distancia(ponto);
        }

        raio = soma / pontos.size();
    }

    @Override
    public FormaGeometrica clonar() {

        Hexagono copia = new Hexagono(centro, raio);

        // Preserva as coordenadas atuais dos seis vértices.
        for (int i = 0; i < pontos.size(); i++) {

            Ponto original = pontos.get(i);
            Ponto destino = copia.pontos.get(i);

            destino.setX(original.getX());
            destino.setY(original.getY());
        }

        copia.setCorPreenchimentoHex(corPreenchimentoHex);
        copia.setCorBordaHex(corBordaHex);
        copia.setEspessuraBorda(espessuraBorda);
        copia.setGroupId(groupId);

        return copia;
    }
}