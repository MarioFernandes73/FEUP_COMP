package cli;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static java.lang.Thread.sleep;

public class WebCrawler
{
    private String jsCode;
    private String jsonCode;
    private String errorMessage;

    public WebCrawler(String jsCode){
        this.jsCode = jsCode;
        this.jsonCode = "";
        this.errorMessage = null;
    }

    public void run(){
        try
        {
            parseJStoJson();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna json que o esprima faz. caso o codigo fonecido tenha erros, a funcao retorna uma empty string.
     * @return
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    public void parseJStoJson() throws UnsupportedEncodingException, FileNotFoundException, InterruptedException {

        String errorReturn = "parse.html";
        String parsedCode = "";
        String website = "http://esprima.org/demo/parse.html";

        File chromeDriver = new File("chromedriver.exe");

        if (!chromeDriver.exists()){
            throw new FileNotFoundException("Web driver não encontrada.");
        }

        System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());

        WebDriver driver = new ChromeDriver();

        driver.get(website);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        Object result =
          js.executeScript(
            "       var code = arguments[0];" +
              "        window.editor.setText(code);" +
              "        setTimeout(function(){ " +
              "           window.location = id('syntax').value; " +
              "        },1000);"

            , jsCode);


        sleep(1100);

        URLDecoder d = new URLDecoder();

        parsedCode = d.decode(driver.getCurrentUrl(), "UTF-8");

        parsedCode = parsedCode.replace(" ", "");
        parsedCode = parsedCode.replace("http://esprima.org/demo/", "");

        System.out.println(parsedCode);

        if (parsedCode.equals(errorReturn)){
            errorMessage = driver.findElement(By.id("info")).getText();
        }
        else{
            jsonCode = parsedCode;
        }
        driver.close();
    }

    public String getJsonCode() {
        return jsonCode;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

}
