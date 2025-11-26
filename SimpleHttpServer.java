import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 处理所有请求
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("服务器已启动，访问地址: http://localhost:" + port);
        System.out.println("按 Ctrl+C 停止服务器");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();

            // 如果是根路径，显示文件列表
            if (requestPath.equals("/")) {
                listFiles(exchange);
                return;
            }

            // 获取请求的文件路径
            Path filePath = Paths.get("." + requestPath);

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                // 文件存在，返回文件内容
                byte[] fileContent = Files.readAllBytes(filePath);

                // 设置Content-Type
                String contentType = getContentType(filePath.toString());
                exchange.getResponseHeaders().set("Content-Type", contentType);

                exchange.sendResponseHeaders(200, fileContent.length);
                OutputStream os = exchange.getResponseBody();
                os.write(fileContent);
                os.close();
            } else {
                // 文件不存在，返回404
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private void listFiles(HttpExchange exchange) throws IOException {
            File dir = new File(".");
            File[] files = dir.listFiles();

            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'><title>文件列表</title></head><body>");
            html.append("<h1>文件列表</h1><ul>");

            if (files != null) {
                for (File file : files) {
                    if (!file.isHidden()) {
                        String name = file.getName();
                        html.append("<li><a href='/").append(name).append("'>")
                            .append(name).append("</a></li>");
                    }
                }
            }

            html.append("</ul></body></html>");

            byte[] response = html.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private String getContentType(String filePath) {
            if (filePath.endsWith(".html") || filePath.endsWith(".htm")) {
                return "text/html; charset=UTF-8";
            } else if (filePath.endsWith(".css")) {
                return "text/css";
            } else if (filePath.endsWith(".js")) {
                return "application/javascript";
            } else if (filePath.endsWith(".json")) {
                return "application/json";
            } else if (filePath.endsWith(".png")) {
                return "image/png";
            } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (filePath.endsWith(".gif")) {
                return "image/gif";
            } else {
                return "text/plain";
            }
        }
    }
}
