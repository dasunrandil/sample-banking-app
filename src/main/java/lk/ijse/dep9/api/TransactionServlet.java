package lk.ijse.dep9.api;

import jakarta.annotation.Resource;
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
import lk.ijse.dep9.dto.AccountDTO;
import lk.ijse.dep9.dto.TransactionDTO;
import lk.ijse.dep9.dto.TransferDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;

@WebServlet(name = "transaction-servlet", urlPatterns = "/transactions/*", loadOnStartup = 0)
public class TransactionServlet extends HttpServlet {

    @Resource(lookup = "java:comp/env/jdbc/dep9-boc")
    private DataSource pool;
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
                if (transactionType.equalsIgnoreCase("withdraw")) {
//                    TransactionDTO transactionDTO = new TransactionDTO("withdraw", jsonObj.getString("account"), jsonObj.getJsonNumber("amount").bigDecimalValue());
//                    withDrawMoney(transactionDTO, resp);
                    TransactionDTO transactionDTO = JsonbBuilder.create().fromJson(json, TransactionDTO.class);
                    withDrawMoney(transactionDTO, resp);

                } else if (transactionType.equalsIgnoreCase("deposit")) {
                    TransactionDTO transactionDTO = JsonbBuilder.create().fromJson(json, TransactionDTO.class);
                    depositMoney(transactionDTO, resp);

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

    private void depositMoney(TransactionDTO transactionDTO, HttpServletResponse resp) throws IOException {
        try {
            if (transactionDTO.getAccount() == null || !transactionDTO.getAccount().matches("[A-Za-z0-9]{8}(-[A-Za-z0-9]{4}){3}-[A-Za-z0-9]{12}")){
                throw new JsonbException("Invalid account number");
            } else if (transactionDTO.getAmount() == null || transactionDTO.getAmount().compareTo(new BigDecimal(100)) < 0) {
                throw new JsonbException("Invalid amount");
            }

            Connection connection = pool.getConnection();
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM Account WHERE account_number=?");
            stm.setString(1, transactionDTO.getAccount());
            ResultSet rst = stm.executeQuery();
            if (!rst.next()){
                throw new JsonbException("Invalid account number");
            }

            try{
                connection.setAutoCommit(false);
                PreparedStatement stmUpdate = connection.prepareStatement("UPDATE Accont SET balance + ? WHERE account_numbe r= ?");
                stmUpdate.setBigDecimal(1, transactionDTO.getAmount());
                stmUpdate.setString(2, transactionDTO.getAccount());
                if(stmUpdate.executeUpdate() != 1) throw new SQLException("Failed to update the balance");

//                if (true)
//                    throw new RuntimeException("Something went wrong");

                PreparedStatement stmNewTransaction = connection.prepareStatement("INSERT INTO Transaction (account, type, description, amount, date) VALUES (?, ?, ?, ?, ?)");
                stmNewTransaction.setString(1, transactionDTO.getAccount());
                stmNewTransaction.setString(2, "CREDIT");
                stmNewTransaction.setString(3, "Deposit");
                stmNewTransaction.setBigDecimal(4, transactionDTO.getAmount());
                stmNewTransaction.setTimestamp(5, new Timestamp(new Date().getTime()));
                if ((stmNewTransaction.executeUpdate() != 1)) throw new SQLException("Failed to add a transaction record");

                connection.commit();

                ResultSet resultSet = stm.executeQuery();
                resultSet.next();
                String name = resultSet.getString("holder_name");
                String address = resultSet.getString("holder_address");
                BigDecimal balance = resultSet.getBigDecimal("balance");
                AccountDTO accountDTO = new AccountDTO(transactionDTO.getAccount(), name, address, balance);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.setContentType("application/json");
                JsonbBuilder.create().toJson(accountDTO, resp.getWriter());
            }catch (Throwable t){
                connection.rollback();
                t.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to deposit the money, contact the bank");
            }finally {
                connection.setAutoCommit(true);
            }

            connection.close();
        } catch (JsonbException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void withDrawMoney(TransactionDTO transactionDTO, HttpServletResponse resp) throws IOException {
        System.out.println("Withdraw money");
        System.out.println(transactionDTO);
    }

    private void transferMoney(TransferDTO transferDTO, HttpServletResponse resp) throws IOException {
        System.out.println("Transfer money");
        System.out.println(transferDTO);
    }
}
