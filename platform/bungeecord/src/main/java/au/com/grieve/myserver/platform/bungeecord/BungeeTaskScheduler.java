/*
 * MIT License
 *
 * Copyright (c) 2020 MyServer Developers
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

package au.com.grieve.myserver.platform.bungeecord;

import au.com.grieve.myserver.api.scheduler.IScheduledTask;
import au.com.grieve.myserver.api.scheduler.ITaskScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
public class BungeeTaskScheduler implements ITaskScheduler {
    private final BungeePlugin plugin;
    private final TaskScheduler realScheduler;

    @Override
    public void cancel(int id) {
        realScheduler.cancel(id);
    }

    @Override
    public void cancel(IScheduledTask task) {
        realScheduler.cancel(((BungeeScheduledTask) task).getRealScheduledTask());

    }

    @Override
    public IScheduledTask runAsync(Runnable runnable) {
        return new BungeeScheduledTask(realScheduler.runAsync(plugin, runnable));
    }

    @Override
    public IScheduledTask schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return new BungeeScheduledTask(realScheduler.schedule(plugin, runnable, delay, timeUnit));
    }

    @Override
    public IScheduledTask schedule(Runnable runnable, long delay, long repeat, TimeUnit timeUnit) {
        return new BungeeScheduledTask(realScheduler.schedule(plugin, runnable, delay, repeat, timeUnit));
    }
}
