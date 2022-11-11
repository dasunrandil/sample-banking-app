package lk.ijse.dep9.api;

import jakarta.annotation.Resource;
import jakarta.json.Json;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.dep9.dto.AccountDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(name = "account-servlet", urlPatterns = "/accounts/*", loadOnStartup = 0)
public class AccountServlet extends HttpServlet {

    @Resource(lookup = "java:comp/env/jdbc/dep9-boc")
    private DataSource pool;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() == null || req.getPathInfo().equals("/")){
            try {
                if (req.getContentType() == null || !req.getContentType().startsWith("application/json")){
                    throw new JsonbException("Invalid JSON");
                }

                AccountDTO accountDTO = JsonbBuilder.create().fromJson(req.getReader(), AccountDTO.class);
                createAccount(accountDTO, resp);
            } catch (JsonbException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }else {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }
    private void createAccount(AccountDTO accountDTO, HttpServletResponse resp) throws IOException {
        try (Connection connection = pool.getConnection()){
            if(accountDTO.getName() == null || !accountDTO.getName().matches("[A-Za-z ]+")){
                throw new JsonbException("Invalid account holder name");
            } else if (accountDTO.getAddress() == null || accountDTO.getAddress().isBlank()) {
                throw new JsonbException("Invalid account holder address");
            }

            accountDTO.setAccount(UUID.randomUUID().toString());
            accountDTO.setBalance(BigDecimal.ZERO);

            PreparedStatement stm = connection.prepareStatement("INSERT INTO Account(account_number, holder_name, holder_address) VALUES (?, ?, ?)");
            stm.setString(1, accountDTO.getAccount());
            stm.setString(2, accountDTO.getName());
            stm.setString(3, accountDTO.getAddress());

            if (stm.executeUpdate() == 1){
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.setContentType("application/json");
                JsonbBuilder.create().toJson(accountDTO, resp.getWriter());
            }else {
                throw new SQLException("Something went wrong, try again");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
