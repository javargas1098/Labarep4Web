
package arem.web;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class Request implements Runnable {

	private Socket clientSocket;

	public Request(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public String header(String status, String resource) {
		return "HTTP/1.1 " + status + " \r\n" + "Content-Type: " + resource + "\r\n\r\n";
	}

	@Override
	public void run() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String inputLine;
			inputLine = in.readLine();
			if (inputLine != null && inputLine.contains("GET")) {
				String[] tempArray = inputLine.split(" ");
				String path;
				BufferedReader br = null;
				try {
					if (tempArray[1].contains(".html")) {
						path = System.getProperty("user.dir") + "/resources" + tempArray[1];
						br = new BufferedReader(new FileReader(path));
						out.write("HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "\r\n");
						out.println();
						String temp = br.readLine();
						while (temp != null) {
							out.write(temp);
							temp = br.readLine();
						}

						br.close();

					} else if (tempArray[1].contains(".png") || tempArray[1].contains(".jpg")) {

						String tipoString = "";
						String tipoString1 = "";
						if (tempArray[1].contains(".png")) {
							tipoString = "PNG";
							tipoString1 = "Content-Type: image/png\r\n";

						} else {
							tipoString = "JPG";
							tipoString1 = "Content-Type: image/jpg\r\n";
						}
						BufferedImage image = ImageIO
								.read(new File(System.getProperty("user.dir") + "/resources" + tempArray[1]));
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write(image, tipoString, baos);
						byte[] imageBy = baos.toByteArray();
						DataOutputStream outImg = new DataOutputStream(clientSocket.getOutputStream());
						outImg.writeBytes("HTTP/1.1 200 OK \r\n");
						outImg.writeBytes(tipoString1);
						outImg.writeBytes("Content-Length: " + imageBy.length);
						outImg.writeBytes("\r\n\r\n");
						outImg.write(imageBy);
						outImg.close();
						out.println(outImg.toString());

					} else if (tempArray[1].substring(1, 4).equals("App")) {
						out.write("HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "\r\n");
						out.println();

						Reflections reflections = new Reflections("arem.web.App",
								new SubTypesScanner(false));

						Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);

						String[] appPath = tempArray[1].split("/");

						for (Object clas : allClasses) {
							String[] pathString = clas.toString().split(" ");
							String[] classString = pathString[1].split("\\.");
							if (appPath[2].equals(classString[3])) {

								Class c = Class.forName(pathString[1]);
								String m = appPath[3].split("=")[0];
								String param = appPath[3].split("=")[1];
								Method metodo = c.getDeclaredMethod(m, String.class);

								if (metodo.isAnnotationPresent(Web.class)) {
									out.write(metodo.invoke(null, param).toString());
								}

								break;
							}

						}

					}
				} catch (IOException e) {
					out.println(header("404 NOT FOUND", "text/html\r\n")
							+ "<h1> 404 PAGE NOT FOUND </h1> <h3> This is not the web page you are looking for. </h3>");
				} catch (Exception ex) {
					Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
					out.println(header("404 NOT FOUND", "text/html\r\n")
							+ "<h1> 404 PAGE NOT FOUND </h1> <h3> This is not the web page you are looking for. </h3>");
				}
			}
			out.close();
			in.close();
			clientSocket.close();
		} catch (IOException ex) {
			Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
