package autopackage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import test.capitalone.framework.CapitalOneTestSuite;

public class LoanCalculator extends CapitalOneTestSuite {
	
	/**
	 * Test case1: Test calculation from excel data.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCalculation() throws Exception {
		
		clickMenuItem("Auto Loan Calculators", "navWrapper");
		
		List<String[]>  testdata = getTestData();  
		
		for (int i = 0; i < testdata.size(); i++) {
			String[] data = testdata.get(i);

			setFieldValue("Loan Amount", data[0]);
			setFieldValue("Estimated APR", data[1]);

			setSelectValue("loanTerm", data[2]);

			assertEquals(getMonthlyPayment(), data[3]);

		}
	}
	
	@Test
	public void testLoanAmountError() throws Exception {

		clickMenuItem("Auto Loan Calculators", "navWrapper");

		setFieldValue("Loan Amount", "5000");
		assertEquals(getFieldError("Loan Amount"), "Must be above $7,500 and below $40,000");
		
		setFieldValue("Loan Amount", "7499");
		assertEquals(getFieldError("Loan Amount"), "Must be above $7,500 and below $40,000");

		setFieldValue("Loan Amount", "40001");
		assertEquals(getFieldError("Loan Amount"), "Must be above $7,500 and below $40,000");

		setFieldValue("Loan Amount", "7500");
		assertTrue(getFieldError("Loan Amount") == null);

		setFieldValue("Loan Amount", "40000");
		assertTrue(getFieldError("Loan Amount") == null);
	}
	
	@Test
	public void testEstimatedAPRError() throws Exception {

		clickMenuItem("Auto Loan Calculators", "navWrapper");

		setFieldValue("Estimated APR", "2.5");
		assertEquals(getFieldError("Estimated APR"), "Please enter an APR above 3.24% and below 24.99%");
		
		setFieldValue("Estimated APR", "25");
		assertEquals(getFieldError("Estimated APR"), "Please enter an APR above 3.24% and below 24.99%");
		
		setFieldValue("Estimated APR", "3.23");
		assertEquals(getFieldError("Estimated APR"), "Please enter an APR above 3.24% and below 24.99%");
		
		setFieldValue("Estimated APR", "3.24");
		assertTrue(getFieldError("Estimated APR") == null);
		
		setFieldValue("Estimated APR", "24.99");
		assertTrue(getFieldError("Estimated APR") == null);
	}
	
	
	
	/**
	 * Get Monthly payment amount.
	 * 
	 * @return
	 */
	private String getMonthlyPayment() throws Exception {

		// wait for calculation
		int j = Integer.parseInt(getProperties().getProperty("waittime.for.calculation"));
		Thread.sleep(j);
		
		// return the text
		return getWebdriver().findElement(By.id("monthlyPaymentIDDesktop")).getText();
	}
	
	/**
	 * Validate properties.
	 * 
	 * @param properties
	 */
	protected void validateProperties(Properties properties) throws Exception {
		
		super.validateProperties(properties);
		checkIfNumber("waittime.for.calculation");
	}
}
