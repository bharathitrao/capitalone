package test.capitalone.framework;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

public class CapitalOneTestSuite {

	private String url = null;
	private Properties properties;
	private WebDriver driver;
	private List<String[]> testdata = null;
	
	/**
	 *  Perform Initializations
	 */
	@BeforeTest
	public void setUp() throws Exception {

		properties = new Properties();
		properties.load(getClass().getResourceAsStream("Configuration.properties"));
		
		// validate
		validateProperties(properties);
		
		// set the properties
		System.setProperty(properties.getProperty("webdriver.name"), properties.getProperty("webdriver.location"));
		
		url = properties.getProperty("url");
		
		// load the test data
		String testDatafile = properties.getProperty("test.data.file");
		int firstrownum = Integer.parseInt(properties.getProperty("test.data.first.rownum", "2"));
		
		testdata = readTestData(testDatafile, firstrownum);
		
		// fire the page
		driver = new ChromeDriver();
		driver.get(url);
		driver.manage().window().maximize();
	}
	
	protected Properties getProperties() {
		return properties;
	}
	
	protected WebDriver getWebdriver() {
		return driver;
	}
	
	/**
	 * Method to validate if the properties are good. Test case classes can override and add more validations.
	 * 
	 * @param properties
	 */
	protected void validateProperties(Properties properties) throws Exception {
		
		checkIfEmpty("webdriver.name");
		checkIfEmpty("webdriver.location");
		checkIfEmpty("url");
		
		checkIfNumber("test.data.first.rownum");
	}
	
	/**
	 * Method to check if the property is a number if entered. Missing is fine.
	 * 
	 * @param name
	 * @throws Exception
	 */
	protected void checkIfNumber(String name) throws Exception {
		String value = properties.getProperty(name);
		if (value == null || value.trim().isEmpty())
			return;
		
		try {
			Integer.parseInt(value);
			
		} catch(NumberFormatException ne) {
			throw new Exception("Property: " + name + " should be a number.");
		}
	}

	/**
	 * Check if the property is configured or not and throw an error.
	 * 
	 * @param name
	 * @throws Exception
	 */
	protected void checkIfEmpty(String name) throws Exception {
		
		String value = properties.getProperty(name);
		if (value == null || value.trim().isEmpty()) {
			throw new Exception("Property: " + name + " is required.");
		}
	}

	/**
	 * Return the test data.
	 * @return
	 */
	public List<String[]> getTestData() {
		return testdata;
	}
	
	/**
	 * Method to cleanup/close after execution.
	 * 
	 */
	@AfterTest
	public void cleanup() {
//		webdriver.close();
	}
	
	
	/**
	 * Set the drop down select value to the displayed text passed.
	 * 
	 * @param fieldid
	 * @param value
	 */
	protected void setSelectValue(String fieldid, String value) {
		
		WebElement e = driver.findElement(By.id(fieldid));
		Select select = new Select(e);
		select.selectByVisibleText(value);
	}
	
	/**
	 * Set the text field value. Clears the previous value!
	 * 
	 * @param fieldname
	 * @param value
	 */
	protected void setFieldValue(String fieldname, String value) {

		if (value == null || value.equals(""))
			return;

		WebElement div = getFieldDiv(fieldname);

		WebElement input = div.findElement(By.tagName("input"));
		input.clear();
		input.sendKeys(value);
	}

	private WebElement getFieldDiv(String fieldname) {
		return driver.findElement(By.xpath("//div[@class='form-group']/label[text()='" + fieldname + "']/.."));
	}
	
	/**
	 * Get the field error message if present, otherwise returns null
	 * 
	 * @param fieldname
	 * @return
	 */
	protected String getFieldError(String fieldname) {

		WebElement div = getFieldDiv(fieldname);
		// get to the parent of the text box, and find the error msg if available
		try {	
			WebElement e = div.findElement(By.xpath("//div[@class='errorTooltip']/span"));

			// no errors for this field
			if (e == null)
				return null;

			// return error msg
			return e.getText();
		} catch (Exception e) {
			// No error msg. may not be present of the field value is good
		}

		return null;
	}

	/**
	 * Navigate to a Menu Item
	 * @param w
	 * @param name
	 */
	protected void clickMenuItem(String name, String idname) {
		WebElement e = driver.findElement(By.xpath("//div[@id='" +idname+ "']"));
		e = e.findElement(By.xpath("//li[@role='menuitem']/a[text()='" + name + "']"));
		driver.navigate().to(e.getAttribute("href"));
		
	}

	/**
	 * Method to read data from input file.
	 * 
	 * @param filepath
	 * @param startrownum
	 * 
	 * @return
	 * @throws IOException
	 */
	protected List<String[]> readTestData(String filepath, int startrownum) throws IOException {

		if (filepath == null)
			return null;

		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(filepath));

		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row;

		Iterator rows = sheet.rowIterator();

		List<String[]> data = new ArrayList<String[]>();
		String[] rowstring = null;
		int rownum = 0;
		DataFormatter formatter = new DataFormatter();

		while (rows.hasNext()) {

			row = (XSSFRow) rows.next();
			rownum++;
			// ignore until the starting row
			if (rownum < startrownum)
				continue;

			rowstring = new String[row.getLastCellNum()];
			for (int i = 0; i < row.getLastCellNum(); i++) {
				rowstring[i] = formatter.formatCellValue(row.getCell(i));
			}
			data.add(rowstring);
		}

		wb.close();
		return data;
	}
}
