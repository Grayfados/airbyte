/*
 * MIT License
 *
 * Copyright (c) 2020 Airbyte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.airbyte.server.migration;

import io.airbyte.db.Database;
import io.airbyte.db.Databases;
import java.io.IOException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public class MockAirbyteDB implements AutoCloseable {

  private final PostgreSQLContainer<?> container;
  private final Database database;

  public Database getDatabase() {
    return database;
  }

  public MockAirbyteDB() throws IOException, InterruptedException {
    container =
        new PostgreSQLContainer<>("postgres:13-alpine")
            .withDatabaseName("airbyte")
            .withUsername("docker")
            .withPassword("docker");
    container.start();

    container
        .copyFileToContainer(MountableFile.forClasspathResource("migration/schema.sql"), "/etc/init.sql");
    // execInContainer uses Docker's EXEC so it needs to be split up like this
    container.execInContainer("psql", "-d", "airbyte", "-U", "docker", "-a", "-f", "/etc/init.sql");

    database = Databases
        .createPostgresDatabase(container.getUsername(), container.getPassword(),
            container.getJdbcUrl());

  }

  @Override
  public void close() throws Exception {
    database.close();
    container.close();
  }

}
