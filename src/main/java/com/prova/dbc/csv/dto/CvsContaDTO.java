package com.prova.dbc.csv.dto;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

import com.prova.dbc.csv.util.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CvsContaDTO {

	private String agencia;
	private String conta;	
	private String saldo;
	private Status status;
	private Boolean retornoReceita;
	
	public Double getSaldoConvertido()  {
		try {
			return DecimalFormat.getNumberInstance( new Locale("pt", "BR")).parse(this.saldo).doubleValue();
		} catch (ParseException e) {
			e.printStackTrace();	
			return null;
		}
	}

	public String getConta() {
		return this.conta.replace("-", "");
	}
}
