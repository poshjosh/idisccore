package com.idisc.core;














public class FeedUpdateService
  extends Service<FeedUpdateTask>
{
  public FeedUpdateTask newTask()
  {
    return new FeedUpdateTask();
  }
  
  public String toString()
  {
    return "Service for running " + FeedUpdateTask.class.getName();
  }
}
