package lk.ijse.dep9.api;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.dep9.dto.TransferDTO;
import lk.ijse.dep9.dto.WithdrawDTO;

import java.io.IOException;
import java.io.StringReader;

@WebServlet(name = "transaction-servlet", urlPatterns = "/transactions/*", loadOnStartup = 0)
public class TransactionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getPathInfo() == null || req.getPathInfo().equals("/")){
            try {
                if(req.getContentType() == null || !req.getContentType().startsWith("application/json")){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
                    return;
                }

//                String json = "";
//                String line = null;
//                while ((line=req.getReader().readLine()) != null){
//                    json += line;
//                }
//                System.out.println(json);

//                StringBuilder sb = new StringBuilder();
//                req.getReader().lines().forEach(line-> sb.append(line));
//                System.out.println(sb.toString());

                String json = req.getReader().lines().reduce("", (p, c) -> p + c);

//                JsonParser parser = Json.createParser(req.getReader());
                JsonParser parser = Json.createParser(new StringReader(json));
                parser.next();
                JsonObject jsonObj = parser.getObject();
                String transactionType = jsonObj.getString("type");
                if (transactionType.equalsIgnoreCase("withdraw")){
//                    WithdrawDTO withdrawDTO = new WithdrawDTO("withdraw", jsonObj.getString("account"), jsonObj.getJsonNumber("amount").bigDecimalValue());
//                    withDrawMoney(withdrawDTO, resp);
                    WithdrawDTO withdrawDTO = JsonbBuilder.create().fromJson(json, WithdrawDTO.class);
                    withDrawMoney(withdrawDTO, resp);
                } else if (transactionType.equalsIgnoreCase("transfer")) {
//                    TransferDTO transferDTO = new TransferDTO("transfer", jsonObj.getString("from"), jsonObj.getString("to"), jsonObj.getJsonNumber("amount").bigDecimalValue());
//                    transferMoney(transferDTO, resp);
                    TransferDTO transferDTO = JsonbBuilder.create().fromJson(json, TransferDTO.class);
                    transferMoney(transferDTO, resp);
                }else {
                    throw new JsonbException("Invalid JSON");
                }
            } catch (JsonbException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            }

        }else {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private void withDrawMoney(WithdrawDTO withdrawDTO, HttpServletResponse resp) throws IOException {
        System.out.println("Withdraw money");
        System.out.println(withdrawDTO);
    }

    private void transferMoney(TransferDTO transferDTO, HttpServletResponse resp) throws IOException {
        System.out.println("Transfer money");
        System.out.println(transferDTO);
    }
}
