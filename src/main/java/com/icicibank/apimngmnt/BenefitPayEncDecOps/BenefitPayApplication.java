package com.icicibank.apimngmnt.BenefitPayEncDecOps;

import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.apigee.flow.execution.Action;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;

/**
 * Hello world!
 *
 */
public class BenefitPayApplication  implements Execution{
	
	private Map<String, String> properties; // read-only

	public BenefitPayApplication(Map<String, String> properties) {
		this.properties = properties;
	}
	public static void main(String[] args) {
		//System.out.println("Hello World!");
		
		//sBenefitPayApplication appObj = new BenefitPayApplication();
		/*
		 * String result=""; try { result=appObj.decryptAES(args[0], args[1]);
		 * System.out.println(result); } catch (Exception e) {
		 * result.concat(e.getMessage()); }
		 */
		
		
	}
	
	@Override
	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {
		try {
			String strOne = resolveVariable(this.properties.get("Key"), messageContext);
			String strTwo = resolveVariable(this.properties.get("Trandata"), messageContext);
			messageContext.setVariable("Key", strOne);
			messageContext.setVariable("Trandata", strTwo);
			String result = decryptAES(strOne,strTwo);
			
			messageContext.setVariable("DecryptedTrandata", result);
			//messageContext.setVariable("stage", stage);
			return ExecutionResult.SUCCESS;
		} catch (Exception ex) {
			ExecutionResult executionResult = new ExecutionResult(false, Action.ABORT);
			executionResult.setErrorResponse(ex.getMessage());
			executionResult.addErrorResponseHeader("ExceptionClass", ex.getClass().getName());
			//messageContext.setVariable("stage", stage);
			messageContext.setVariable("JAVA_ERROR", ex.getMessage());
			messageContext.setVariable("JAVA_STACKTRACE", ex.getClass().getName());
			return ExecutionResult.ABORT;
		}
	}
	private String resolveVariable(String variable, MessageContext msgContext) {
	    if (variable.isEmpty())
	      return ""; 
	    if (!variable.startsWith("{") || !variable.endsWith("}"))
	      return variable; 
	    String value = msgContext.getVariable(variable.substring(1, variable.length() - 1)).toString();
	    if (value.isEmpty())
	      return variable; 
	    return value;
	  }

	public String decryptAES(String key, String encryptedString) throws Exception {
		String AES_IV = "PGKEYENCDECIVSPC";
		SecretKeySpec skeySpec = null;
		IvParameterSpec ivspec = null;
		Cipher cipher = null;
		byte[] textDecrypted = null;
		try {
			byte[] b = decodeHexString(encryptedString);
			skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			ivspec = new IvParameterSpec(AES_IV.getBytes("UTF-8"));
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
			textDecrypted = cipher.doFinal(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			skeySpec = null;
			ivspec = null;
			cipher = null;
		}
		return (new String(textDecrypted));
	}

	public byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	public byte hexToByte(String hexString) {
		int firstDigit = toDigit(hexString.charAt(0));
		int secondDigit = toDigit(hexString.charAt(1));
		return (byte) ((firstDigit << 4) + secondDigit);
	}

	private int toDigit(char hexChar) {
		int digit = Character.digit(hexChar, 16);
		if (digit == -1) {
			throw new IllegalArgumentException("Invalid Hexadecimal Character: " + hexChar);
		}
		return digit;
	}

	
}
