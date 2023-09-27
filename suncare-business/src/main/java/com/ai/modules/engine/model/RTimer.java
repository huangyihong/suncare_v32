package com.ai.modules.engine.model;

import java.util.concurrent.TimeUnit;

public class RTimer {
	public static final int STARTED = 0;
	public static final int STOPPED = 1;
	public static final int PAUSED = 2;
	protected int state;
	private TimerImpl timerImpl;
	private double time;
	private double culmTime;

	private class NanoTimeTimerImpl implements TimerImpl {
		private long start;

		private NanoTimeTimerImpl() {
		}

		public void start() {
			this.start = System.nanoTime();
		}

		public double elapsed() {
			return TimeUnit.MILLISECONDS.convert(System.nanoTime() - this.start, TimeUnit.NANOSECONDS);
		}
	}

	protected TimerImpl newTimerImpl() {
		return new NanoTimeTimerImpl();
	}

	public RTimer() {
		this.time = 0.0D;
		this.culmTime = 0.0D;
		this.timerImpl = newTimerImpl();
		this.timerImpl.start();
		this.state = 0;
	}

	public double stop() {
		assert this.state == 0 || this.state == 2;
		this.time = this.culmTime;
		if (this.state == 0)
			this.time += this.timerImpl.elapsed();
		this.state = 1;
		return this.time;
	}

	public void pause() {
		assert this.state == 0;
		this.culmTime += this.timerImpl.elapsed();
		this.state = 2;
	}

	public void resume() {
		if (this.state == 0)
			return;
		assert this.state == 2;
		this.state = 0;
		this.timerImpl.start();
	}

	public double getTime() {
		if (this.state == 1)
			return this.time;
		if (this.state == 2)
			return this.culmTime;

		assert this.state == 0;
		return this.culmTime + this.timerImpl.elapsed();
	}

	protected static interface TimerImpl {
		void start();

		double elapsed();
	}
}
