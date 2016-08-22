package org.homesweethome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.ClientProtocolException;

/**
 * This utility class is used to annotate documents using TagME RestfulAPI
 *
 * Additional help https://tagme.d4science.org/tagme/tagme_help.html
 *
 * @author Renato Stoffalette Joao
 * @version 1.0
 * @since 2016-04
 */
public class RestHttpClient {

	private static String production = "http://tagme.di.unipi.it/api";
	public static void main(String[] args) throws ClientProtocolException,
			IOException, TransformerException {
		String key = "abc9310bb"; // key - required - The alphanumeric code to
									// access this service. Send an email to
									// tagme@di.unipi.it to ask for a temporary
									// key.
		String lang = "en"; // lang - optional - The language of the text to be
							// annotated accepted values are "it" for Italian
							// and "en" for English. Default is "en".
		File inputFile = new File(args[0]);

		for (File fileEntry : inputFile.listFiles()) {
			StringBuffer textBuff = new StringBuffer();
			String text = null;
			BufferedReader br = new BufferedReader(new FileReader(fileEntry));
			String line;
			while ((line = br.readLine()) != null) {
				textBuff.append(line);
				textBuff.append("\n");
			}
			br.close();
			text = textBuff.toString();
			Charset.forName("UTF-8").encode(text);
			if (text.isEmpty()) {
				continue;
			}
			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod(production);
			method.setRequestHeader("ContentType",
					"application/x-www-form-urlencoded;charset=UTF-8");
			method.setRequestHeader("Accept", "text/xml");
			method.addParameter("key", key);
			method.addParameter("text", text);
			method.addParameter("lang", lang);
			method.addParameter("epsilon", "0.5"); // epsilon - This parameter
													// can be used to finely
													// tune the disambiguation
													// process: an higher value
													// will favor the
													// most-common topics for a
													// spot, whereas a lower
													// value will take more into
													// account the context.
			StringBuffer outputXMLString = new StringBuffer();
			int statusCode;
			try {
				statusCode = client.executeMethod(method);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: "
							+ method.getStatusLine());
				}

				InputStream rstream = null;
				rstream = method.getResponseBodyAsStream();
				br = new BufferedReader(new InputStreamReader(rstream));
				while ((line = br.readLine()) != null) {
					outputXMLString.append(line);
				}

				PrintWriter outputFileWriter = new PrintWriter(new File(
						fileEntry.getAbsolutePath().concat(".ann")));
				String outXML = outputXMLString.toString();
				Source xmlInput = new StreamSource(new StringReader(outXML));
				StringWriter stringWriter = new StringWriter();
				StreamResult xmlOutput = new StreamResult(stringWriter);
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				transformerFactory.setAttribute("indent-number", 2);
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(xmlInput, xmlOutput);
				xmlOutput.getWriter().toString();
				outputFileWriter.print(xmlOutput.getWriter().toString());
				outputFileWriter.flush();
				outputFileWriter.close();
				br.close();
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}