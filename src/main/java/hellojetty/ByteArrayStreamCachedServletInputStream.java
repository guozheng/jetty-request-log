package hellojetty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ByteArrayStreamCachedServletInputStream extends ServletInputStream {
  private final ByteArrayInputStream input;
  private boolean finished = false;

  public ByteArrayStreamCachedServletInputStream(ByteArrayOutputStream cachedBytes) {
    this.input = new ByteArrayInputStream(cachedBytes.toByteArray());
  }

  @Override
  public int read() throws IOException {
    final int count = input.read();
    this.finished = true;
    return count;
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
}
