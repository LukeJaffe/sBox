package box.gpio;

public interface GPIOInterface
{
    void lockCall();
    void lockCallback(int result);

    void unlockCall();
    void unlockCallback(int result);
}
