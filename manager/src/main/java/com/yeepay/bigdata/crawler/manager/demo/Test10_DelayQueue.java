package com.yeepay.bigdata.crawler.manager.demo;

import java.util.concurrent.DelayQueue;
    import java.util.concurrent.Delayed;
    import java.util.concurrent.TimeUnit;

    public class Test10_DelayQueue {

       private static final TimeUnit delayUnit = TimeUnit.MILLISECONDS;
       private static final TimeUnit ripeUnit = TimeUnit.NANOSECONDS;

       static long startTime;

       static class Task implements Delayed {    
          public long ripe;
          public String name;    
          public Task(String name, int delay) {
             this.name = name;
             ripe = System.nanoTime() + ripeUnit.convert(delay, delayUnit);
          }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof Task) {
            return compareTo((Task) obj) == 0;
         }
         return false;
      }

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (int) (this.ripe ^ (this.ripe >>> 32));
         hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
      }

      @Override
      public int compareTo(Delayed delayed) {
         if (delayed instanceof Task) {
            Task that = (Task) delayed;
             return Long.valueOf(this.ripe).compareTo(that.ripe);
//            return (int) (this.ripe - that.ripe);
         }
         throw new UnsupportedOperationException();
      }

      @Override
      public long getDelay(TimeUnit unit) {
         return unit.convert(ripe - System.nanoTime(), ripeUnit);
      }

      @Override
      public String toString() {
         return "task " + name + " due in " + String.valueOf(getDelay(delayUnit) + "ms");
          }
       }

       static class TaskAdder implements Runnable {

      DelayQueue dq;
      int delay;

      public TaskAdder(DelayQueue dq, int delay) {
         this.dq = dq;
         this.delay = delay;
      }

      @Override
      public void run() {
         try {
            Thread.sleep(delay);

            Task z = new Task("Z", 0);
            dq.add(z);

            Long elapsed = System.currentTimeMillis() - startTime;

            System.out.println("time = " + elapsed + "\tadded " + z);

         } catch (InterruptedException e) {
         }
      }
    }

    public static void main(String[] args) {
      startTime = System.currentTimeMillis();
      DelayQueue<Task> taskQ = new DelayQueue<Task>();

      Thread thread = new Thread(new TaskAdder(taskQ, 3000));
      thread.start();

      taskQ.add(new Task("A", 0));
//      taskQ.add(new Task("A", 1111110));
      taskQ.add(new Task("B", 10));
//      taskQ.add(new Task("B", 11111110));
//      taskQ.add(new Task("C", 100));
      taskQ.add(new Task("C", 1111100));
//      taskQ.add(new Task("D", 1000));
        Task taskD = new Task("D", 1111111000);
//      taskQ.add(new Task("D", 1111111000));
      taskQ.add(taskD);
      taskQ.add(new Task("E", 10000));
      taskQ.add(new Task("F", 100000));

      System.out.println("------initial tasks ---------------");
      Task[] tasks = taskQ.toArray(new Task[0]);
      for (int i = 0; i < tasks.length; i++) {
         System.out.println(tasks[i]);
      }

      System.out.println("------processing--------------------");
      try {
         Long elapsed = System.currentTimeMillis() - startTime;
         while (elapsed < 15000) {
//            Task task = taskQ.poll(1, TimeUnit.SECONDS);
             Task task = taskQ.take();
             boolean isOk = taskQ.remove(taskD);
             System.out.println("isOK : " + isOk);
//             Task task = null;
            elapsed = System.currentTimeMillis() - startTime;
            if (task != null) {
               System.out.println("time = " + elapsed + "\t" + task);
            } else {
//                System.out.println("");
            }
         }

         System.out.println("------remaining after " + elapsed + "ms -----------");
         tasks = taskQ.toArray(new Task[0]);
         for (int i = 0; i < tasks.length; i++) {
            System.out.println(tasks[i]);
         }

//      } catch (InterruptedException e) {
      } catch (Exception e) {
      }
    }
    }