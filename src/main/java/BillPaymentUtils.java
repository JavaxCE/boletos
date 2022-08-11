import static java.lang.Integer.parseInt;
import static java.lang.Math.round;
import static java.lang.String.valueOf;

/**
 * Classe para auxiliar na tratativa de pagamento de conta
 *
 * @author johnatan.cavalcante
 * @since 1.0.0
 */
public final class BillPaymentUtils {

    /**
     * Verificar se é uma conta de consumo: <br>
     * SANEAMENTO <br>
     * ENERGIA ELETRICA/GAS <br>
     * TELECOMUNICACOES
     *
     * @param barCode Código de Barras
     * @return Boolean com a validação
     * @since 1.0.0
     */
    public static Boolean isConsumptionAccount(String barCode) {
        return BarCodeSegmentEnum.getIsConsumptionAccounts().contains(getSegmentId(barCode));
    }

    /**
     * Retorna o segmento do boleto de acordo com o código de barras
     *
     * @param barCode Código de Barras
     * @return BarCodeSegmentEnum segmento do boleto
     * @since 1.0.0
     */
    public static BarCodeSegmentEnum getSegmentId(String barCode) {
        return BarCodeSegmentEnum.from(barCode.substring(1, 2));
    }

    /**
     * Verifica se a linha digitável do pagamento de Convênios / Concessionárias é valido seguindo
     * as regras de geração do FEBRABAN.
     *
     * @param digitable Linha Digitavel
     * @return Flag de validação da linha digitável
     *
     * @see #getModule10(String)
     * @see #getModule11(String)
     *
     * @since 1.0.0
     */
    public static Boolean validateDigitableLineConcessionarie(String digitable) {
        StringBuilder digitableBuilder = new StringBuilder(digitable.replaceAll("\\D", ""));
        while (digitableBuilder.length() < 48) {
            digitableBuilder.append("0");
        }
        digitable = digitableBuilder.toString();

        String[] blocks = new String[4];
        blocks[0] = digitable.substring(0, 12);
        blocks[1] = digitable.substring(12, 24);
        blocks[2] = digitable.substring(24, 36);
        blocks[3] = digitable.substring(36, 48);
        int valid = 0;

        String currencyCode = getCurrencyCode(digitable);

        for (int i = 0; i < blocks.length; i++) {
            if (getModule(currencyCode) == 11) {
                if (validateModule11(blocks[i])) {
                    valid++;
                }
            } else {
                if(validateModule10(blocks[i])) {
                    valid++;
                }
            }
        }

        return valid == 4;
    }

    /**
     * Retorna o tipo do módulo para calculo do digito verificador <br>
     * Módulo 10 <br>
     * Módulo 11
     *
     * @param currencyCode Código da Moeda
     * @return Tipo do Módulo para o DV
     *
     * @since 1.0.0
     */
    public static int getModule(String currencyCode) {
        if (currencyCode.equals("8") || currencyCode.equals("9")) {
            return 11;
        }

        return 10;
    }

    /**
     * Retorna o código da moeda da linha digitavel
     *
     * @param barCode Código de Barras ou Linha Digitável
     * @return Código da Moeda
     *
     * @since 1.0.0
     */
    public static String getCurrencyCode(String barCode) {
        return barCode.substring(2, 3);
    }

    /**
     * Verifica se a linha digitável do pagamento de Consumo / Fatura ou Carnê é valida seguindo
     * as regras de geração do FEBRABAN.
     *
     * @param digitable Linha Digitavel
     * @return Flag de validação da linha digitável
     *
     * @see #getModule10(String)
     *
     * @since 1.0.0
     */
    public static Boolean validateDigitableLineBillPayment(String digitable) {
        digitable = digitable.replaceAll("\\D", "");

        String[] blocks = new String[3];
        blocks[0] = digitable.substring(0, 10);
        blocks[1] = digitable.substring(10, 21);
        blocks[2] = digitable.substring(21, 32);

        int valid = 0;
        for (String block : blocks) {
            if (validateModule10(block)) {
                valid++;
            }
        }

        return valid == 3;
    }

    /**
     * Faz Validação do Bloco da Linha Digitável no Módulo 10
     *
     * @param block Bloco da Linha Digitavel para validação
     * @return Flag de validação do Módulo 10
     *
     * @see #getModule10(String)
     *
     * @since 1.0.0
     */
    public static Boolean validateModule10(String block) {
        String verifyingDigit = valueOf(block.charAt(block.length() - 1));
        return parseInt(verifyingDigit) == getModule10(block.substring(0, block.length() - 1));
    }

    /**
     * Retorona o digito verificador do bloco seguindo as regras do FEBRABAN do Módulo 10
     *
     * @param block  Bloco da Linha Digitavel
     * @return O digito verificador
     *
     * @since 1.0.0
     */
    public static int getModule10(String block) {
        int module = 10;
        int blockSize = block.length();
        String code = block.substring(0, blockSize);
        code = new StringBuilder(code).reverse().toString();
        String[] parts = code.split("");
        int value = 0;

        for (int i = 0; i < parts.length; i++) {
            int sum = parseInt(parts[i]) * (i % 2 == 0 ? 2 : 1);

            while (sum > 9) {
                String[] results = Integer.toString(sum).split("");
                sum = parseInt(results[0]) + parseInt(results[1]);
            }

            value += sum;
        }

        int next = round((float) (value / 10)) * module;
        if (next < value) {
            next += 10;
        }

        return (next - value);
    }

    /**
     * Faz Validação do Bloco da Linha Digitável no Módulo 11
     *
     * @param block Bloco da Linha Digitavel para validação
     * @return Flag de validação do Módulo 11
     *
     * @see #getModule11(String)
     *
     * @since 1.0.0
     */
    public static Boolean validateModule11(String block) {
        int dv = Integer.parseInt(block.substring(block.length() - 1));
        return dv == getModule11(block.substring(0, block.length() - 1));
    }

    /**
     * Retorna o digito verificador do bloco seguindo as regras do FEBRABAN do Módulo 11
     *
     * @param block  Bloco da Linha Digitavel
     * @return O digito verificador
     *
     * @since 1.0.0
     */
    public static int getModule11(String block) {
        int moduleValue = 11;
        int size = 2;
        int total = 0;
        int counter =  block.length();
        int module;

        while (counter > 0) {
            total += Integer.parseInt(block.substring(counter - 1, counter)) * size;
            counter--;
            size++;
            if (size > 9) {
                size = 2;
            }
        }

        module = total % moduleValue;
        if (module < 2) {
            return 0;
        } else {
            return moduleValue - module;
        }
    }


    /**
     *
     * Converte o código de barras em linha digitavel de uma conta de Arrecadação
     *
     * @param barCode Código de Barras
     * @return Linha Digitavel
     *
     * @see #getModule10(String)
     * @see #getModule11(String)
     *
     * @since 1.0.0
     */
    public static String barCodeToDigitableLineConcessionarie(String barCode) {
        barCode = barCode.replaceAll("\\D", "");
        String currencyCode = getCurrencyCode(barCode);
        int module = getModule(currencyCode);

        String field1 = barCode.substring(0, 11);
        String field2 = barCode.substring(11, 22);
        String field3 = barCode.substring(22, 33);
        String field4 = barCode.substring(33, 44);

        return field1 + typedLineCheckDigit(field1, module) + field2
                + typedLineCheckDigit(field2, module) + field3
                + typedLineCheckDigit(field3, module) + field4
                + typedLineCheckDigit(field4, module);
    }

    /**
     *
     * Converte o código de barras em linha digitavel de uma conta de consumo / fatura
     *
     * @param barCode Código de Barras
     * @return Linha Digitavel
     *
     * @see #getModule10(String)
     *
     * @since 1.0.0
     */
    public static String barCodeToDigitableLineBillPayment(String barCode) {
        barCode = barCode.replaceAll("\\D", "");

        final StringBuilder digitableLine = new StringBuilder();

        // Field 1
        // Bank
        digitableLine.append(barCode, 0, 3);

        // Currency
        digitableLine.append(barCode.charAt(3));
        digitableLine.append(barCode, 19, 24);
        digitableLine.append(getModule10(digitableLine.toString()));

        // Field 2
        digitableLine.append(barCode, 24, 34);
        digitableLine.append(getModule10(barCode.substring(24, 34)));

        // Field 3
        digitableLine.append(barCode, 34, 44);
        digitableLine.append(getModule10(barCode.substring(34, 44)));

        // Field 4 -> General DV
        digitableLine.append(barCode.charAt(4));

        // Field 5
        digitableLine.append(barCode, 5, 19);

        return digitableLine.toString();
    }

    /**
     * Adiciona o digito verificador no bloco de acordo com o tipo do modulo
     *
     * @param field Bloco do código de barras
     * @param module Tipo de Módulo de calculo
     * @return Bloco com digito incluso
     *
     * @see #getModule10(String)
     * @see #getModule11(String)
     *
     * @since 1.0.0
     */
    private static String typedLineCheckDigit(String field, Integer module) {
        if (module == 10) {
            return valueOf(getModule10(field));
        }
        return valueOf(getModule11(field));
    }
}
