package org.example.Views;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javafx.scene.paint.Color;

import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Salva e carrega os desenhos do editor em JSON.
 * A leitura valida o documento inteiro antes de devolver as formas,
 * para que um arquivo inválido não apague o desenho que já está aberto.
 */
public final class PersistenciaJson {

    private static final int VERSAO_ARQUIVO = 1;
    private static final int LIMITE_FORMAS = 10000;
    private static final int LIMITE_PONTOS_POR_FORMA = 10000;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private PersistenciaJson() {
    }

    public static void salvar(
            Path destino,
            List<FormaGeometrica> formas,
            double larguraCanvas,
            double alturaCanvas,
            boolean snapHabilitado
    ) throws IOException {

        if (destino == null) {
            throw new IOException("Nenhum caminho de arquivo foi escolhido.");
        }

        String json = gerarJson(
                formas,
                larguraCanvas,
                alturaCanvas,
                snapHabilitado
        );

        Path absoluto = destino.toAbsolutePath().normalize();
        Path pasta = absoluto.getParent();

        if (pasta == null || !Files.isDirectory(pasta)) {
            throw new IOException("A pasta de destino não existe ou não pode ser acessada.");
        }

        String nome = absoluto.getFileName() == null
                ? "editor"
                : absoluto.getFileName().toString();
        String prefixo = "." + nome;
        if (prefixo.length() < 3) {
            prefixo = ".editor";
        }

        Path temporario = null;
        try {
            temporario = Files.createTempFile(pasta, prefixo, ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(
                    temporario,
                    StandardCharsets.UTF_8
            )) {
                writer.write(json);
            }

            try {
                Files.move(
                        temporario,
                        absoluto,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(
                        temporario,
                        absoluto,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {
            if (temporario != null) {
                Files.deleteIfExists(temporario);
            }
        }
    }

    /** Retorna JSON estável para comparar se o desenho mudou após o último salvamento. */
    public static String gerarJson(
            List<FormaGeometrica> formas,
            double larguraCanvas,
            double alturaCanvas,
            boolean snapHabilitado
    ) {
        DocumentoJson documento = criarDocumento(
                formas,
                larguraCanvas,
                alturaCanvas,
                snapHabilitado
        );
        return GSON.toJson(documento);
    }

    public static DocumentoCarregado carregar(Path origem) throws IOException {

        if (origem == null || !Files.isRegularFile(origem)) {
            throw new IOException("O arquivo selecionado não existe ou não é um arquivo válido.");
        }

        if (!Files.isReadable(origem)) {
            throw new IOException("Não há permissão para ler o arquivo selecionado.");
        }

        final DocumentoJson documento;
        try (Reader reader = Files.newBufferedReader(origem, StandardCharsets.UTF_8)) {
            JsonElement raiz = JsonParser.parseReader(reader);

            if (raiz == null || !raiz.isJsonObject()) {
                throw new IOException("O conteúdo não é um documento JSON válido do editor.");
            }

            JsonObject objeto = raiz.getAsJsonObject();
            if (!objeto.has("versao")
                    || !objeto.has("larguraCanvas")
                    || !objeto.has("alturaCanvas")
                    || !objeto.has("snapHabilitado")
                    || !objeto.has("formas")
                    || !objeto.get("formas").isJsonArray()) {
                throw new IOException("O documento JSON não contém todos os campos obrigatórios do editor.");
            }

            documento = GSON.fromJson(raiz, DocumentoJson.class);

        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException(
                    "O arquivo contém JSON inválido ou corrompido. O desenho atual foi preservado.",
                    ex
            );
        }

        validarDocumento(documento);

        List<FormaGeometrica> formasCarregadas = new ArrayList<>();
        for (int i = 0; i < documento.formas.size(); i++) {
            try {
                formasCarregadas.add(construirForma(documento.formas.get(i)));
            } catch (IOException | RuntimeException ex) {
                throw new IOException(
                        "Não foi possível carregar a forma número " + (i + 1)
                                + ". O arquivo pode estar corrompido. O desenho atual foi preservado.",
                        ex
                );
            }
        }

        return new DocumentoCarregado(
                documento.larguraCanvas,
                documento.alturaCanvas,
                documento.snapHabilitado,
                formasCarregadas
        );
    }

    private static DocumentoJson criarDocumento(
            List<FormaGeometrica> formas,
            double larguraCanvas,
            double alturaCanvas,
            boolean snapHabilitado
    ) {
        DocumentoJson documento = new DocumentoJson();
        documento.versao = VERSAO_ARQUIVO;
        documento.larguraCanvas = larguraCanvas;
        documento.alturaCanvas = alturaCanvas;
        documento.snapHabilitado = snapHabilitado;
        documento.formas = new ArrayList<>();

        if (formas == null) {
            throw new IllegalArgumentException("A lista de formas não está disponível.");
        }
        if (formas.size() > LIMITE_FORMAS) {
            throw new IllegalArgumentException("O desenho contém formas demais para ser salvo.");
        }
        if (!Double.isFinite(larguraCanvas) || larguraCanvas <= 0
                || larguraCanvas > 10000
                || !Double.isFinite(alturaCanvas) || alturaCanvas <= 0
                || alturaCanvas > 10000) {
            throw new IllegalArgumentException("As dimensões do Canvas são inválidas.");
        }

        for (FormaGeometrica forma : formas) {
            documento.formas.add(converterParaJson(forma));
        }

        return documento;
    }

    private static FormaJson converterParaJson(FormaGeometrica forma) {
        if (forma == null) {
            throw new IllegalArgumentException("O desenho contém uma forma vazia.");
        }

        FormaJson dados = new FormaJson();

        if (forma instanceof Circulo circulo) {
            Ponto centro = circulo.getCentro();
            if (centro == null) {
                throw new IllegalArgumentException("Um círculo não possui centro válido.");
            }
            dados.tipo = circulo.isCisalhado() ? "CIRCULO_CISALHADO" : "CIRCULO";
            dados.centroX = centro.getX();
            dados.centroY = centro.getY();
            dados.raio = circulo.getRaio();
        } else if (forma instanceof Quadrado) {
            dados.tipo = "QUADRADO";
        } else if (forma instanceof Hexagono) {
            dados.tipo = "HEXAGONO";
        } else {
            dados.tipo = "POLIGONO";
        }

        List<Ponto> pontos = forma.getPontos();
        if (pontos == null || pontos.size() > LIMITE_PONTOS_POR_FORMA) {
            throw new IllegalArgumentException("Uma forma contém uma lista de vértices inválida.");
        }
        switch (dados.tipo) {
            case "POLIGONO", "CIRCULO_CISALHADO" -> {
                if (pontos.size() < 3) {
                    throw new IllegalArgumentException("Uma forma fechada precisa de pelo menos três vértices.");
                }
            }
            case "QUADRADO" -> {
                if (pontos.size() != 4) {
                    throw new IllegalArgumentException("O quadrado não contém quatro vértices.");
                }
            }
            case "HEXAGONO" -> {
                if (pontos.size() != 6) {
                    throw new IllegalArgumentException("O hexágono não contém seis vértices.");
                }
            }
            case "CIRCULO" -> {
                if (!Double.isFinite(dados.raio) || dados.raio <= 0) {
                    throw new IllegalArgumentException("O círculo possui raio inválido.");
                }
            }
            default -> throw new IllegalArgumentException("Tipo de forma não reconhecido: " + dados.tipo);
        }

        dados.pontos = new ArrayList<>();
        for (Ponto ponto : pontos) {
            if (ponto == null
                    || !Double.isFinite(ponto.getX())
                    || !Double.isFinite(ponto.getY())) {
                throw new IllegalArgumentException("Uma forma possui coordenadas inválidas.");
            }
            PontoJson p = new PontoJson();
            p.x = ponto.getX();
            p.y = ponto.getY();
            dados.pontos.add(p);
        }

        dados.corPreenchimento = forma.getCorPreenchimentoHex();
        dados.corBorda = forma.getCorBordaHex();
        dados.espessuraBorda = forma.getEspessuraBorda();
        dados.groupId = forma.getGroupId();

        validarCor(dados.corPreenchimento, "preenchimento");
        validarCor(dados.corBorda, "borda");
        if (!Double.isFinite(dados.espessuraBorda)
                || dados.espessuraBorda <= 0
                || dados.espessuraBorda > 100) {
            throw new IllegalArgumentException("Uma forma possui espessura de borda inválida.");
        }

        return dados;
    }

    private static void validarDocumento(DocumentoJson documento) throws IOException {
        if (documento == null) {
            throw new IOException("O arquivo está vazio ou não contém um documento do editor.");
        }
        if (documento.versao != VERSAO_ARQUIVO) {
            throw new IOException("Versão do arquivo não reconhecida. Não foi possível abrir o projeto.");
        }
        if (!Double.isFinite(documento.larguraCanvas)
                || documento.larguraCanvas < 100
                || documento.larguraCanvas > 10000
                || !Double.isFinite(documento.alturaCanvas)
                || documento.alturaCanvas < 100
                || documento.alturaCanvas > 10000) {
            throw new IOException("As dimensões do Canvas no arquivo são inválidas.");
        }
        if (documento.formas == null || documento.formas.size() > LIMITE_FORMAS) {
            throw new IOException("A lista de formas está ausente ou excede o limite permitido.");
        }
    }

    private static FormaGeometrica construirForma(FormaJson dados) throws IOException {
        if (dados == null || dados.tipo == null || dados.pontos == null) {
            throw new IOException("Os dados de uma forma estão incompletos.");
        }
        if (dados.pontos.size() > LIMITE_PONTOS_POR_FORMA) {
            throw new IOException("Uma forma contém vértices demais.");
        }

        List<Ponto> pontos = new ArrayList<>();
        for (PontoJson p : dados.pontos) {
            if (p == null || p.x == null || p.y == null
                    || !Double.isFinite(p.x) || !Double.isFinite(p.y)) {
                throw new IOException("Uma forma contém coordenadas ausentes ou inválidas.");
            }
            pontos.add(new Ponto(p.x, p.y));
        }

        FormaGeometrica forma;
        switch (dados.tipo) {
            case "POLIGONO" -> {
                exigirQuantidadeMinima(pontos, 3, "polígono");
                Poligono poligono = new Poligono();
                for (Ponto p : pontos) {
                    poligono.adicionarPonto(p);
                }
                forma = poligono;
            }
            case "CIRCULO" -> {
                if (!Double.isFinite(dados.centroX)
                        || !Double.isFinite(dados.centroY)
                        || !Double.isFinite(dados.raio)
                        || dados.raio <= 0) {
                    throw new IOException("O círculo possui centro ou raio inválido.");
                }
                forma = new Circulo(
                        new Ponto(dados.centroX, dados.centroY),
                        dados.raio
                );
            }
            case "CIRCULO_CISALHADO" -> {
                // O modelo atual não expõe um método para restaurar diretamente
                // a flag de cisalhamento. Por isso, preservamos a geometria e o
                // visual como polígono usando os pontos exatos salvos.
                exigirQuantidadeMinima(pontos, 3, "círculo cisalhado");
                Poligono contorno = new Poligono();
                for (Ponto p : pontos) {
                    contorno.adicionarPonto(p);
                }
                forma = contorno;
            }
            case "QUADRADO" -> {
                if (pontos.size() != 4) {
                    throw new IOException("O quadrado deve conter quatro vértices.");
                }
                Ponto centro = centroMedio(pontos);
                double raio = centro.distancia(pontos.get(0));
                if (!Double.isFinite(raio) || raio <= 0) {
                    throw new IOException("O quadrado possui dimensões inválidas.");
                }
                Quadrado quadrado = new Quadrado(centro, raio);
                substituirPontos(quadrado, pontos);
                forma = quadrado;
            }
            case "HEXAGONO" -> {
                if (pontos.size() != 6) {
                    throw new IOException("O hexágono deve conter seis vértices.");
                }
                Ponto centro = centroMedio(pontos);
                double raio = centro.distancia(pontos.get(0));
                if (!Double.isFinite(raio) || raio <= 0) {
                    throw new IOException("O hexágono possui dimensões inválidas.");
                }
                Hexagono hexagono = new Hexagono(centro, raio);
                substituirPontos(hexagono, pontos);
                forma = hexagono;
            }
            default -> throw new IOException("O arquivo contém um tipo de forma desconhecido: " + dados.tipo);
        }

        validarCor(dados.corPreenchimento, "preenchimento");
        validarCor(dados.corBorda, "borda");
        if (!Double.isFinite(dados.espessuraBorda)
                || dados.espessuraBorda <= 0
                || dados.espessuraBorda > 100) {
            throw new IOException("Uma forma possui espessura de borda inválida.");
        }

        forma.setCorPreenchimentoHex(dados.corPreenchimento);
        forma.setCorBordaHex(dados.corBorda);
        forma.setEspessuraBorda(dados.espessuraBorda);
        forma.setGroupId(dados.groupId);

        return forma;
    }

    private static void substituirPontos(FormaGeometrica forma, List<Ponto> pontos) {
        List<Ponto> destino = forma.getPontos();
        destino.clear();
        for (Ponto ponto : pontos) {
            destino.add(ponto);
        }
    }

    private static Ponto centroMedio(List<Ponto> pontos) {
        double x = 0;
        double y = 0;
        for (Ponto ponto : pontos) {
            x += ponto.getX();
            y += ponto.getY();
        }
        return new Ponto(x / pontos.size(), y / pontos.size());
    }

    private static void exigirQuantidadeMinima(
            List<Ponto> pontos,
            int minimo,
            String nomeForma
    ) throws IOException {
        if (pontos.size() < minimo) {
            throw new IOException("O " + nomeForma + " possui vértices insuficientes.");
        }
    }

    private static void validarCor(String cor, String propriedade) {
        if (cor == null || cor.isBlank()) {
            throw new IllegalArgumentException("A cor de " + propriedade + " está ausente.");
        }
        try {
            Color.web(cor);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("A cor de " + propriedade + " é inválida: " + cor, ex);
        }
    }

    public static final class DocumentoCarregado {
        public final double larguraCanvas;
        public final double alturaCanvas;
        public final boolean snapHabilitado;
        public final List<FormaGeometrica> formas;

        private DocumentoCarregado(
                double larguraCanvas,
                double alturaCanvas,
                boolean snapHabilitado,
                List<FormaGeometrica> formas
        ) {
            this.larguraCanvas = larguraCanvas;
            this.alturaCanvas = alturaCanvas;
            this.snapHabilitado = snapHabilitado;
            this.formas = formas;
        }
    }

    private static final class DocumentoJson {
        int versao;
        double larguraCanvas;
        double alturaCanvas;
        boolean snapHabilitado;
        List<FormaJson> formas = new ArrayList<>();
    }

    private static final class FormaJson {
        String tipo;
        List<PontoJson> pontos = new ArrayList<>();
        double centroX;
        double centroY;
        double raio;
        String corPreenchimento;
        String corBorda;
        double espessuraBorda;
        String groupId;
    }

    private static final class PontoJson {
        Double x;
        Double y;
    }
}
