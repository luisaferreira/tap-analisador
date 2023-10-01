import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        final String caminhoDiretorio = sc.nextLine();
        // D:\Github\Projetos\tap-analisador\vikings-first-season
        // C:\vikings-first-season
        sc.close();

        var diretorio = new File(caminhoDiretorio);

        // Verificando se o caminho que o usuário digitou existe e se é um diretório
        // válido
        if (diretorio.exists() && diretorio.isDirectory()) {
            // Listando os arquivos existentes dentro do diretório
            var arquivos = diretorio.listFiles();

            // Verificando se existem arquivos no diretório
            if (arquivos.length > 0) {
                // Filtrando os arquivos para obter apenas os de extensão .srt
                var arquivosSrt = Arrays.stream(arquivos)
                        .filter(x -> x.isFile() && x.getName().endsWith(".srt"))
                        .toList();

                // Verificando se o diretório possui arquivos com a extensão .srt
                if (arquivosSrt.size() > 0) {
                    var contadoresTemporada = new ArrayList<Contador>();
                    for (File arquivoSrt : arquivosSrt) {
                        var contadores = LerArquivoSrt(arquivoSrt);
                        contadoresTemporada.addAll(0, contadores);

                        var nomeArquivo = "episodio-"
                                + ObterNomeArquivoSemExtensao(arquivoSrt.getName()) + ".json";

                        var caminhoArquivo = Paths.get("resultados");

                        if (!Files.exists(caminhoArquivo)) {
                            Files.createDirectory(caminhoArquivo);
                        }

                        caminhoArquivo = Paths.get(caminhoArquivo.toString(), nomeArquivo);
                        EscreverArquivoJson(caminhoArquivo.toString(), contadores);
                    }

                    var mapContador = contadoresTemporada.stream()
                            .collect(Collectors.toMap(
                                    Contador::getPalavra,
                                    Contador::getFrequencia,
                                    Integer::sum));

                    var novosContadores = mapContador.entrySet().stream()
                            .map(x -> new Contador(x.getKey(), x.getValue()))
                            .collect(Collectors.toList());

                    novosContadores.sort(Comparator.comparingInt(Contador::getFrequencia).reversed());

                    var pastaTemp = caminhoDiretorio.split("\\\\");
                    var nomeArquivoTemp = "temporada-" + pastaTemp[pastaTemp.length - 1] + ".json";
                    var caminhoArquivoTemp = Paths.get("resultados", nomeArquivoTemp);
                    EscreverArquivoJson(caminhoArquivoTemp.toString(), novosContadores);

                } else {
                    System.out.println("Não há arquivos com a extensão .srt disponiveis nesse diretório!");
                }
            } else {
                System.out.println("Não há arquivos disponíveis nesse diretório!");
            }

        }
    }

    public static List<Contador> LerArquivoSrt(File arquivo) {
        try {
            var fileInputStream = new FileInputStream(arquivo);
            // Criando um array de bytes para armazenar o contéudo do arquivo
            var conteudoArquivoBytes = new byte[(int) arquivo.length()];
            // Lendo o conteúdo do arquivo para o array de bytes
            fileInputStream.read(conteudoArquivoBytes);
            fileInputStream.close();
            // Convertendo o array de bytes em uma string
            var conteudoArquivo = new String(conteudoArquivoBytes);
            // Retirando os caracteres indesejáveis da string
            conteudoArquivo = conteudoArquivo.replaceAll("<.*?>", "")
                    .replaceAll("<\\s*/?\\s*i\\s*>", "")
                    .replaceAll("[^a-zA-Z'\\s]", "")
                    .replaceAll("\\s+", " ")

                    .trim();

            // Isolando as palavras pelo espaço para criar um array
            var arrayConteudoArquivo = conteudoArquivo.split(" ");

            var listaContador = Arrays.stream(arrayConteudoArquivo)
                    .map(x -> x.toLowerCase())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .map(x -> new Contador(x.getKey(), x.getValue().intValue()))
                    .sorted(Comparator.comparingLong(Contador::getFrequencia).reversed())
                    .toList();

            return listaContador;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void EscreverArquivoJson(String caminhoArquivo, List<Contador> listaContadores) {
        try {

            var conteudo = FormatarContador(listaContadores);

            var fWriter = new FileWriter(caminhoArquivo);
            fWriter.write(conteudo);
            fWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static String ObterNomeArquivoSemExtensao(String nomeArquivo) {
        return nomeArquivo.substring(0, nomeArquivo.length() - 4);
    }

    public static String FormatarContador(List<Contador> listaContadores) {
        var sBuilder = new StringBuilder();

        sBuilder.append("[");

        for (Contador contador : listaContadores) {
            sBuilder.append("\n  {");
            sBuilder.append("\n     \"palavra\": \"" + contador.getPalavra() + "\"");
            sBuilder.append("\n     \"frequencia\": " + contador.getFrequencia());
            sBuilder.append("\n  },");
        }
        sBuilder.append("\n]");

        return sBuilder.toString();
    }
}

class Contador implements Comparable<Contador> {
    private String Palavra;
    private int Frequencia;

    public Contador(String palavra, int frequencia) {
        this.Palavra = palavra;
        this.Frequencia = frequencia;
    }

    public String getPalavra() {
        return this.Palavra;
    }

    public int getFrequencia() {
        return this.Frequencia;
    }

    @Override
    public int compareTo(Contador contador) {
        if (this.Frequencia > contador.getFrequencia())
            return -1;
        else if (this.Frequencia < contador.getFrequencia())
            return 1;
        else
            return 0;
    }
}
