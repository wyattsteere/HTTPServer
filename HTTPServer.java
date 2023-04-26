package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class HTTPServer {
    private static String[] requestParts;
    private static BufferedReader in;
    private static int fileLength;
    private static BufferedImage image;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);

        while (true) {
            Socket clientSocket = serverSocket.accept();

            new Thread(() -> {
                try {
                    handleRequest(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestLine = in.readLine();
        try {
            requestParts = requestLine.split(" ");
        } catch(NullPointerException e) {
            System.out.println("Postman sends an initial request without a requestLine, please try again");
        }
        String httpMethod = requestParts[0];
        String url = requestParts[1];

        if (httpMethod.equals("GET")) {
            String responseBody = handleGETRequest();
            System.out.println(responseBody);
            if (responseBody.equals("")) {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println("\r\n");
                out.println("404 Not Found");
            } else if (responseBody.equals("jpg")) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: image/jpeg");
                out.println("Content-Length: " + fileLength);
                out.println("\r\n");
                out.println(image);
            }
            else {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + (fileLength + 2));
                out.println("\r\n");
                out.println(responseBody);
            }
        } else if (httpMethod.equals("DELETE")) {
            String responseBody = handleDELETERequest();
            if (responseBody.equals("No File")) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/HTTP");
                out.println("\r\n");
                out.println("Content Deleted");
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println("\r\n");
                out.println("404 Not Found");
            }
        } else if (httpMethod.equals("POST")) {
            String result = handlePOSTRequest();
            if (result.equals("X")) {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println("\r\n");
                out.println("404 Not Found");
            } else {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/HTTP");
                out.println("\r\n");
                out.println("Content Appended");
            }
        } else if (httpMethod.equals("PUT")) {
            String result = handlePUTRequest();
            if (result == "X") {
                out.println("HTTP/1.1 308 Not Modified");
                out.println("Content-Type: text/plain");
                out.println("\r\n");
                out.println("308 Not Modified");
            } else {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/HTTP");
                out.println("\r\n");
                out.println("Content Created or Replaced");
            }
        }
         /*else {
            // Return a 405 error for any other HTTP method
            out.println("HTTP/1.1 405 Method Not Allowed");
            out.println("Content-Type: text/plain");
            out.println("\r\n");
            out.println("405 Method Not Allowed");
        }*/

        in.close();
        out.close();
        clientSocket.close();
    }

    private static String handleGETRequest() throws IOException {
        String file1 = "";
        String url = requestParts[1];
        String[] headerParts = url.split("/");
        String fileRequest = headerParts[headerParts.length - 1];
        String pathName = "C:\\Users\\wyatt\\IdeaProjects\\practice\\HTTPServer\\src\\main\\java\\org\\example\\" + fileRequest;
        File getRequest = new File("C:\\Users\\wyatt\\IdeaProjects\\practice\\HTTPServer\\src\\main\\java\\org\\example\\exampleski.jpg");
        if (getRequest.exists() && fileRequest.contains(".txt")) {
            FileInputStream inFile = new FileInputStream(getRequest);
            fileLength = (int) getRequest.length();
            System.out.println(fileLength);
            byte Bytes[] = new byte[fileLength];
            System.out.println("File size is: " + inFile.read(Bytes));
            file1 = new String(Bytes);
            System.out.println("File content is:\n" + file1);
            inFile.close();
            return file1;
        } else if (getRequest.exists() && fileRequest.contains(".jpg")) {
            image = ImageIO.read(new File(pathName));
            return file1 = "jpg";
        }
        return file1;
    }

    private static String handleDELETERequest() {
        String file1 = "";
        String url = requestParts[1];
        String[] headerParts = url.split("/");
        String fileRequest = headerParts[headerParts.length - 1];
        File getRequest = new File("C:\\Users\\wyatt\\IdeaProjects\\practice\\HTTPServer\\src\\main\\java\\org\\example\\" + fileRequest);
        if (getRequest.delete()) {
            System.out.println("Deleted request File");
        } else {
            return file1 = "No File";
        }
        return file1 = "Deleted File";
    }

    private static String handlePOSTRequest() throws IOException {
        String result = "";
        String url = requestParts[1];
        String[] headerParts = url.split("/");
        String fileRequest = headerParts[headerParts.length - 1];
        File getRequest = new File("C:\\Users\\wyatt\\IdeaProjects\\practice\\HTTPServer\\src\\main\\java\\org\\example\\" + fileRequest);
        if (getRequest.exists()) {
            int contentLength = 0;
                for (String line = in.readLine(); !line.isEmpty(); line = in.readLine()) {
                    if (line.startsWith("Content-Length: ")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length: ".length()));
                    }
                }
            char[] buffer = new char[contentLength];

            String postData = new String(buffer);
            System.out.println(postData);
            BufferedWriter outFile = new BufferedWriter(
                    new FileWriter(getRequest, true));
            outFile.write(postData);
            outFile.close();
        } else {
            result = "X";
        }
        return result;
    }

    private static String handlePUTRequest() throws IOException {
        String result = "";
        String url = requestParts[1];
        String[] headerParts = url.split("/");
        String fileRequest = headerParts[headerParts.length - 1];
        File getRequest = new File("C:\\Users\\wyatt\\IdeaProjects\\practice\\HTTPServer\\src\\main\\java\\org\\example\\" + fileRequest);
            int contentLength = 0;
            for (String line = in.readLine(); !line.isEmpty(); line = in.readLine()) {
                if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length: ".length()));
                }
            }
            char[] buffer = new char[contentLength];
            String putData = new String(buffer);
            System.out.println(putData);
            try {
                BufferedWriter out = new BufferedWriter(
                        new FileWriter(getRequest));
                out.write(putData);

                out.close();
            } catch (NullPointerException e){
                result = "X"; //sparks response code
        }
        return result;
    }
}
