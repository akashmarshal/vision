package com.vision.authentication;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import jespa.util.LogStream;

class TimestampedLogStream extends LogStream
{
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  TimestampedLogStream(OutputStream out)
  {
    super(out);
  }

  public void println(Object o)
  {
    synchronized (this.sdf) {
      super.println(this.sdf.format(new Date()) + ": " + o);
    }
  }

  public void println(String s) {
    synchronized (this.sdf) {
      super.println(this.sdf.format(new Date()) + ": " + s);
    }
  }
}
