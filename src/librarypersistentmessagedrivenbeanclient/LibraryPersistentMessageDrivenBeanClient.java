/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package librarypersistentmessagedrivenbeanclient;
    
import com.tutorialspoint.stateless.LibraryPersistentMessageDrivenBeanRemote;
import com.tutorialspoint.entity.Book;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;



public class LibraryPersistentMessageDrivenBeanClient {

    BufferedReader brConsoleReader = null;
    Properties props;
    InitialContext ctx;

    {

        props = new Properties();
        try {
            props.load(new FileInputStream("jndi.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            ctx = new InitialContext(props);
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
        brConsoleReader
                = new BufferedReader(new InputStreamReader(System.in));
    }

    public static void main(String[] args) {

        LibraryPersistentMessageDrivenBeanClient ejbTester = new LibraryPersistentMessageDrivenBeanClient();

        ejbTester.testMessageBeanEjb();
    }

    private void showGUI() {
        System.out.println("**********************");
        System.out.println("Welcome to Book Store");
        System.out.println("**********************");
        System.out.print("Options \n1. Add Book\n2. Exit \nEnter Choice: ");
    }

    private void testMessageBeanEjb() {

        try {
            int choice = 1;
            Queue queue = (Queue) ctx.lookup("/queue/BookQueue");
            QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
            QueueConnection connection = factory.createQueueConnection();
            QueueSession session
                    = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            while (choice != 2) {
                String bookName;
                showGUI();
                String strChoice = brConsoleReader.readLine();
                choice = Integer.parseInt(strChoice);
                if (choice == 1) {
                    System.out.print("Enter book name: ");
                    bookName = brConsoleReader.readLine();
                    Book book = new Book();
                    book.setName(bookName);
                    ObjectMessage objectMessage
                            = session.createObjectMessage(book);
                    sender.send(objectMessage);
                } else if (choice == 2) {
                    break;
                }
            }

            LibraryPersistentMessageDrivenBeanRemote libraryBean
                    = (LibraryPersistentMessageDrivenBeanRemote) ctx.lookup("LibraryPersistentMessageDrivenBean/remote");

            List<Book> booksList = libraryBean.getBooks();

            System.out.println("Book(s) entered so far: " + booksList.size());
            int i = 0;
            for (Book book : booksList) {
                System.out.println((i + 1) + ". " + book.getName());
                i++;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (brConsoleReader != null) {
                    brConsoleReader.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
