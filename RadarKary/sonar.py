import time
from flask import Flask
import RPi.GPIO as GPIO
import time


# -----------------------
# Define some functions
# -----------------------

def measure():
    # This function measures a distance
    GPIO.output(GPIO_TRIGGER, True)
    time.sleep(0.00001)
    GPIO.output(GPIO_TRIGGER, False)
    start = time.time()

    while GPIO.input(GPIO_ECHO) == 0:
        start = time.time()

    while GPIO.input(GPIO_ECHO) == 1:
        stop = time.time()

    elapsed = stop - start
    distance = (elapsed * 34300) / 2

    return distance


def measure_average():
    # This function takes 3 measurements and
    # returns the average.
    distance1 = measure()
    time.sleep(0.1)
    distance2 = measure()
    time.sleep(0.1)
    distance3 = measure()
    distance = distance1 + distance2 + distance3
    distance = distance / 3
    return distance


app = Flask(__name__)

GPIO.setmode(GPIO.BCM)

# Define GPIO to use on Pi
GPIO_TRIGGER = 23
GPIO_ECHO = 24
GPIO_LED = 25

dlim = 30.0

# Set pins as output and input
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)  # Trigger
GPIO.setup(GPIO_ECHO, GPIO.IN)  # Echo
GPIO.setup(GPIO_LED, GPIO.OUT)  # Led

# Set trigger to False (Low)
GPIO.output(GPIO_TRIGGER, False)


@app.route('/')
def hello_world():
    try:
    	return str(measure_average())
    except Exception:
        return 1000

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=9999)
    GPIO.cleanup()
