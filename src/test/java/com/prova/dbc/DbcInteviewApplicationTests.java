package com.prova.dbc;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.opencsv.exceptions.CsvException;

@SpringBootTest
class DbcInteviewApplicationTests {

	@Test
	@DisplayName("Teste da sincronização")
	void test_sincroizacao() throws CsvException {
		DbcInteviewApplication.main(new String[] {"./conta.csv"});
        File arquivo = new File("./conta_com_retorno.csv");
        arquivo.exists();
        Assertions.assertThat(arquivo).isFile();
	}
}
