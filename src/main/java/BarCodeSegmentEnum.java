import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Segmentos do Código de Barras
 *
 * @author johnatan.cavalcante
 * @since 1.0.0
 */
public enum BarCodeSegmentEnum {

    /**
     * PREFEITURAS
     */
    CITY_HALLS("1", "Prefeituras", false),

    /**
     * SANEAMENTO
     */
    SANITATION("2", "Saneamento", true),

    /**
     * ENERGIA ELETRICA/GAS
     */
    ENERGY_ELECTRICAL_GAS("3", "Energia Elétrica e Gás", true),

    /**
     * TELECOMUNICACOES
     */
    TELECOMMUNICATIONS("4", "Telecomunicações", true),

    /**
     * ORGAOS GOVERNAMENTAIS
     */
    GOVERNMENT_AGENCIES("5", "Órgãos Governamentais", false),

    /**
     * CARNES / ASSEMELHADOS DEMAIS
     */
    MEAT_OTHERS("6", "Carnes e Assemelhados ou demais. Empresas/Órgãos que serão identificadas através do CNPJ.", false),

    /**
     * MULTAS TRANSITO
     */
    TRAFFIC_FINES("7", "Multas de trânsito", false),

    /**
     * USO EXCLUSIVO BANCO
     */
    BANK_USE_EXCLUSIVE("8", "Uso exclusivo do banco", false);

    private String code;
    private String description;

    private Boolean isConsumptionAccount;

    BarCodeSegmentEnum(String code, String description, Boolean isConsumptionAccount) {
        this.code = code;
        this.description = description;
        this.isConsumptionAccount = isConsumptionAccount;
    }

    public static List<BarCodeSegmentEnum> getIsConsumptionAccounts() {
        return stream(values())
                .filter(consumption -> consumption.isConsumptionAccount)
                .collect(toList());
    }

    public static BarCodeSegmentEnum from(String code) {
        return stream(values())
                .filter(consumption -> consumption.code.equals(code))
                .findFirst().orElse(null);
    }
}
