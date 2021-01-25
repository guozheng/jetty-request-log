package hellojetty.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class CachedBodyServletInputStream extends ServletInputStream {
  private final ByteArrayInputStream input;
  private boolean finished = false;

  public CachedBodyServletInputStream(ByteArrayInputStream input) {
    this.input = input;
  }

  @Override
  public boolean isFinished() {
    return this.finished;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {

  }

  @Override
  public int read() throws IOException {
    this.finished = true;
    return input.read();
  }
}
