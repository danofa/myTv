package org.damcode.www.mytv;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.damcode.www.mytv.db.MongoSingleton;

/**
 *
 * @author dm
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/"})
public class MainVC extends HttpServlet {

    FtvHandler ftv;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ftv = new FtvHandler();
    }

    @Override
    public void destroy() {
        MongoSingleton.INSTANCE.shutdown();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestPath = request.getServletPath().toLowerCase();
        HttpSession session = request.getSession();

        session.setAttribute("baseurl", FtvHandler.baseUrl);

        if (requestPath.matches("/add") && request.getParameter("_id") != null) {
            ftv.addShow(request.getParameter("_id"));

        } else if (requestPath.matches("/seen") && request.getParameter("eid") != null && request.getParameter("_id") != null) {
            ftv.setWatched(request.getParameter("_id"), request.getParameter("eid"));

        } else if (requestPath.matches("/update")) {
            ftv.updateShowsData();
        } 
        
        session.setAttribute("nextepisodes", ftv.getAllNextEpisodes());
        session.setAttribute("unwatchedshows", ftv.getAllUnwatched());
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
