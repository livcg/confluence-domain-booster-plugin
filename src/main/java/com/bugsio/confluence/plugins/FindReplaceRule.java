package com.bugsio.confluence.plugins;

public class FindReplaceRule {

	private String f;
	private String r;
	
	public String getF() {
		return f;
	}
	public void setF(String f) {
		this.f = f;
	}
	public String getR() {
		return r;
	}
	public void setR(String r) {
		this.r = r;
	}
	
	@Override
	public String toString() {
		return "[f=" + f + ", r=" + r + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((f == null) ? 0 : f.hashCode());
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FindReplaceRule other = (FindReplaceRule) obj;
		if (f == null) {
			if (other.f != null)
				return false;
		} else if (!f.equals(other.f))
			return false;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		return true;
	}
		
}
