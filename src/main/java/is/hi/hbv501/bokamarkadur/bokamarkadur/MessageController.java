package is.hi.hbv501.bokamarkadur.bokamarkadur;

import is.hi.hbv501.bokamarkadur.bokamarkadur.Entities.Book;
import is.hi.hbv501.bokamarkadur.bokamarkadur.Entities.Message;
import is.hi.hbv501.bokamarkadur.bokamarkadur.Entities.User;
import is.hi.hbv501.bokamarkadur.bokamarkadur.Services.BookService;
import is.hi.hbv501.bokamarkadur.bokamarkadur.Services.MessageService;
import is.hi.hbv501.bokamarkadur.bokamarkadur.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
public class MessageController {

    private UserService userService;
    private BookService bookService;
    private MessageService messageService;

    @Autowired
    public MessageController(UserService userService, BookService bookService, MessageService messageService) {
        this.userService = userService;
        this.bookService = bookService;
        this.messageService = messageService;
    }

    /*
     * Býr til nýtt message með textanum sem maður skrifar í boxið :D
     */
    @RequestMapping(value ="/messageBook/{id}", method = RequestMethod.POST)
    public String messageBook(@PathVariable("id") long id, @Valid Message message,
                              Model model, HttpSession session) {

        System.out.println("ID á new Message í request message POST: " + message.getId());
        Book book = bookService.findById(id).orElseThrow(()-> new IllegalArgumentException("Invalid book ID"));
        model.addAttribute("book", book);
        model.addAttribute("message", message);
        User sessionUser = (User) session.getAttribute("LoggedInUser");
        model.addAttribute("loggedIn", sessionUser);
        User current = userService.findByUsername(sessionUser.getUsername());
        message.setBook(book);
        message.setSender(current);
        message.setReceiver(book.getUser());
        messageService.save(message);
        System.out.println("ID á new Message í request message POST eftir save: " + message.getId());
        return "messageSuccess";
    }

    /*
     * Opnar síðu með message boxi :D
     */
    @RequestMapping(value="/messageBook/{id}", method = RequestMethod.GET)
    public String sendRequest(@PathVariable("id") long id, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("LoggedInUser");
        model.addAttribute("loggedIn", sessionUser);
        if (sessionUser == null) {
            return "please-log-in";
        }
        Book book = bookService.findById(id).orElseThrow(()-> new IllegalArgumentException("Invalid book ID"));
        model.addAttribute("book", book);
        Message message = new Message();
        System.out.println("ID á new Message í request message GET: " + message.getId());
        model.addAttribute("message", message);
        return "messageBox";
    }


    @RequestMapping(value="/myMessages", method = RequestMethod.GET)
    public String viewMessages(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("LoggedInUser");
        model.addAttribute("loggedIn", sessionUser);
        if (sessionUser == null) {
            return "please-log-in";
        }
        User current = userService.findByUsername(sessionUser.getUsername());
        List<Message> receivedMessages = messageService.findByReceiver(current);
        //Prófa að prenta
        System.out.println("Received messages:");
        for (int i = 0; i < receivedMessages.size(); i++) {
            System.out.println(receivedMessages.get(i) + " hefur ID: " + receivedMessages.get(i).getId());
        }
        model.addAttribute("receivedmessages", receivedMessages);

        List<Message> sentMessages = messageService.findBySender(current);
        //Prófa að prenta
        System.out.println("Sent messages:");
        for (int i = 0; i < sentMessages.size(); i++) {
            System.out.println(sentMessages.get(i) + " hefur ID: " + sentMessages.get(i).getId());
        }
        model.addAttribute("sentmessages", sentMessages);
        return "myMessages";
    }

    // Opnar síðu þar sem þú getur svarað skilaboði sem þú ýttir á
    @RequestMapping(value="/replyMessage/{id}", method = RequestMethod.GET)
    public String pushReply(@PathVariable("id") long id, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("LoggedInUser");
        model.addAttribute("loggedIn", sessionUser);
        if (sessionUser == null) {
            return "please-log-in";
        }
        // Skilaboðin sem þú ert að svara
        Message initialMessage = messageService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid message ID"));
        // Ný tóm skilaboð inn í módelið -> Reply skilaboðin sem þú ætlar að skrifa
        Message newMessage = new Message();
        model.addAttribute("newMessage", newMessage);
        model.addAttribute("initialMessage", initialMessage);
        return "replyMessage";
    }

    // Sendir reply skilaboð sem þú skrifar
    @RequestMapping(value="/replyMessage/{id}", method = RequestMethod.POST)
    public String sendReply(@PathVariable("id") long id, @Valid Message newMessage, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("LoggedInUser");
        model.addAttribute("loggedIn", sessionUser);
        if (sessionUser == null) {
            return "please-log-in";
        }
        // Skilaboðin sem þú ert að svara
        Message initialMessage = messageService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid message ID"));
        model.addAttribute("initialMessage", initialMessage);
        // Ný skilaboð sett inn af módelinu
        String newMessageBody = newMessage.messageBody;
        newMessage = new Message();
        newMessage.setMessageBody(newMessageBody);
        model.addAttribute("newMessage", newMessage);
        newMessage.setBook(initialMessage.getBook());
        User current = userService.findByUsername(sessionUser.getUsername());
        newMessage.setSender(current);
        newMessage.setReceiver(initialMessage.getSender());
        //messageService.save(initialMessage);
        messageService.save(newMessage);
        return "redirect:/myMessages";
    }


}
