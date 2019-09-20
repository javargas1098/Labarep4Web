package arem.web.App;

import arem.web.Web;

/**
 * @author Javier Vargas
 * POJO calculadora
 */
public class Calculadora {
	/**
	 * Este metodo eleva un numero al cuadrdado 
	 * 
	 * @param data String
	 *   
	 * Es el dato recibido por parametro 
	 * 
	 * @return Integer num
	 * 
	 */
	@Web()
	public static Integer elevado(String number) {
		Integer num = Integer.parseInt(number);
		return num*num;
	}
}
