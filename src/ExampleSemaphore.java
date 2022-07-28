import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/*
 *  объект класса Semaphore нужен для ограничения потоков к какому-либо ресурсу
 *  Semaphore semaphore = new Semaphore(3); // максимум 3 потока могут одновременно пользоваться ресурсом
 *  semaphore.acquire(); // метод acquire() даёт разрешение на пользование ресурсом в начале пользования
 *  semaphore.release(); // метод release() снимает разрешения на пользование ресурсом в конце пользования
 *  System.out.println(semaphore.availablePermits()); // метод availablePermits() вызвращает количество оставшихся разрешений
 */

public class ExampleSemaphore {
    public static void main(String[] args) throws InterruptedException {
        // создание 300 потоков
        ExecutorService executorService = Executors.newFixedThreadPool(300);
        // пример задания для потоков
        Connection connection = Connection.getConnection();
        for (int i = 0; i < 300; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        connection.work();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        // окончание выпонения заданий
        executorService.shutdown();
        // ожидание окончания выполнения потоками всех заданий (1 день)
        executorService.awaitTermination(1, TimeUnit.DAYS);
    }
}
// класс Connection как пример соединения к серверу, где сервер это ресурс;
// класс Connection представляет собой паттерн Singleton, из-за того что в классе имеется приватный конструктор
// и пользователю разрешено создавать только один объект вне данного класса
class Connection {
    private static Connection connection = new Connection();
    private int connectionsCount;
    // создание объекта семафора для ограничения использования сервера 10 потоками одновременно
    private Semaphore semaphore = new Semaphore(10);
    private Connection() {
    }
    public static Connection getConnection() {
        return connection;
    }

    public void work() throws InterruptedException {
        semaphore.acquire();
//        // стандартная форма выполнения
//        doWork();
//        semaphore.release();
        // форма выпонения на случай непредвиденных ситуаций в методе doWork()
        try {
            doWork();
        } finally {
            semaphore.release();
        }
    }

    private void doWork() throws InterruptedException {
        synchronized (this) {
            connectionsCount++;
            System.out.println(connectionsCount);
        }

        Thread.sleep(5000); // пример моделирования полезной работы

        synchronized (this) {
            connectionsCount--;
            System.out.println(connectionsCount);
        }
    }

}
