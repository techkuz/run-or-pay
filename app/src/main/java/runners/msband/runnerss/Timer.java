package runners.msband.runnerss;
import android.os.CountDownTimer;

public class Timer {
        private static Timer instance;

        private CountDownTimer timer;

        private Timer(){}

        public void setData(CountDownTimer _timer){
            this.timer = _timer;
        }
        public CountDownTimer getData(){
            return this.timer;
        }

        public static synchronized Timer getInstance(){
            if(instance==null){
                instance=new Timer();
            }
            return instance;
        }
    }
