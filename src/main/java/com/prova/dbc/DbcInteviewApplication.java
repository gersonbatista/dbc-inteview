package com.prova.dbc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.prova.dbc.csv.dto.CvsContaDTO;
import com.prova.dbc.csv.service.ReceitaService;

@SpringBootApplication
public class DbcInteviewApplication {

	private static ReceitaService receitaService;
	private static Logger logger = LoggerFactory.getLogger(DbcInteviewApplication.class);
	private static final char SEPARATOR = ';';

	@Autowired
	public DbcInteviewApplication(ReceitaService receitaService) {
		DbcInteviewApplication.receitaService = receitaService;
	}

	public static void main(String[] args) throws CsvException {
		SpringApplication.run(DbcInteviewApplication.class, args);

		try {
			logger.info("Iniciando a sincronização");
			logger.info("Caminho do arquivo {}", args[0]);
			
			Reader reader = Files.newBufferedReader(Paths.get(args[0]));

			CsvToBean<CvsContaDTO> csvConta = new CsvToBeanBuilder<CvsContaDTO>(reader).withType(CvsContaDTO.class)
					.withSeparator(SEPARATOR).build();

			List<CvsContaDTO> lstContas = csvConta.parse();

			List<CvsContaDTO> lstContasComRetorno = enviaArquivoCscParaReceita(lstContas);
			salvaRetornoEmArquivoCsv(lstContasComRetorno);

			logger.info("Fim da sincronização");

		} catch (IOException e) {
			logger.error("Erro ao ler o arquivo csv");
		} catch (RuntimeException re) {
			logger.error("Aconteceu um erro ao tentar sincronizar: {}", re.getMessage());
		} catch (InterruptedException ie) {
			logger.error("Aconteceu um erro ao tentar sincronizar: {}", ie.getMessage());
		}
	}

	private static List<CvsContaDTO> enviaArquivoCscParaReceita(List<CvsContaDTO> lstContas)
			throws InterruptedException {
		
		List<CvsContaDTO> lstContasComRetorno = new ArrayList<>();
		
		for (CvsContaDTO cvsConta : lstContas) {
			logger.info("Inicio do envio para a receita da agencia/conta: {} {}", cvsConta.getAgencia(), cvsConta.getConta());

			boolean retornoReceita = receitaService.atualizarConta(cvsConta.getAgencia(), cvsConta.getConta(),
					cvsConta.getSaldoConvertido(), cvsConta.getStatus().name());
			
			cvsConta.setRetornoReceita(retornoReceita);
			lstContasComRetorno.add(cvsConta);
		}
		return lstContasComRetorno;
	}

	private static void salvaRetornoEmArquivoCsv(List<CvsContaDTO> lstContasComRetorno) {
		logger.info("Salvando arquivo com resposta");
		
		try {
			Writer writer = Files.newBufferedWriter(Paths.get("conta_com_retorno.csv"));
			StatefulBeanToCsv<CvsContaDTO> sbc = new StatefulBeanToCsvBuilder<CvsContaDTO>(writer)
					.withSeparator(SEPARATOR).build();

			sbc.write(lstContasComRetorno);
			writer.close();

		} catch (IOException ioe) {
			logger.error("Erro ao salvar o arquivo de retorno: {}", ioe.getMessage());
		} catch (CsvDataTypeMismatchException csve) {
			logger.error("Erro ao converter o objeto: {}", csve.getMessage());
		} catch (CsvRequiredFieldEmptyException csvr) {
			logger.error("Erro ao converter campo: {}", csvr.getMessage());
		}
	}
}
