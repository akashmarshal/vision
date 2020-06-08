package examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamsExample {

	public static void main(String[] args) {
		List<ProfileVb> profileList = insertData();
		execute(profileList);
	}

	private static void execute(List<ProfileVb> profileList) {
		List<String> names = Arrays.asList("Reflection", "Collection", "Stream");
		List<String> result = (List) names.stream().filter(s -> s.startsWith("S")).collect(Collectors.toList());
		System.out.println("size :" + result != null ? result.size() : null);

		String conditionalUserGrp = "";
		List<ProfileVb> treeStructuredProfile = new ArrayList<ProfileVb>();
		for (ProfileVb profileVb : profileList) {
			if (!conditionalUserGrp.equalsIgnoreCase(profileVb.getUserGrp())) {
				String tempUserGrp = profileVb.getUserGrp();
				List<ProfileVb> filteredProfile = (List<ProfileVb>) profileList.stream()
						.filter(pd -> pd.getUserGrp().equalsIgnoreCase(tempUserGrp)).collect(Collectors.toList());
				conditionalUserGrp = tempUserGrp;
				treeStructuredProfile.add(new ProfileVb(conditionalUserGrp, "", filteredProfile));
			}
		}

	}

	private static List<ProfileVb> insertData() {
		List<ProfileVb> profileList = new ArrayList<ProfileVb>();
		profileList.add(new ProfileVb("ADMIN", "ADMINUSER", null));
		profileList.add(new ProfileVb("ADMIN", "MAKER", null));
		profileList.add(new ProfileVb("ADMIN", "MANAGEMENT", null));
		profileList.add(new ProfileVb("ADMIN", "MISTEAM", null));
		profileList.add(new ProfileVb("ADMIN", "TRAINEE", null));
		profileList.add(new ProfileVb("AUDIT", "REPORTUSER", null));
		profileList.add(new ProfileVb("BRANCH", "MANAGEMENT", null));
		profileList.add(new ProfileVb("BUSINESS", "CFOS", null));
		profileList.add(new ProfileVb("BUSINESS", "FINANCE", null));
		profileList.add(new ProfileVb("BUSINESS", "MANAGEMENT", null));
		profileList.add(new ProfileVb("BUSINESS", "TRAINEE", null));
		profileList.add(new ProfileVb("CMU", "REPORTUSER", null));
		profileList.add(new ProfileVb("CREDIT", "REPORTUSER", null));
		profileList.add(new ProfileVb("FINANCE", "FINANCE", null));
		profileList.add(new ProfileVb("FINANCE", "MANAGEMENT", null));
		profileList.add(new ProfileVb("HR", "REPORTUSER", null));
		profileList.add(new ProfileVb("JAVATEAM", "SENIORENGG", null));
		profileList.add(new ProfileVb("OPERATIONS", "MANAGEMENT", null));
		profileList.add(new ProfileVb("OPERATIONS", "REPORTUSER", null));
		profileList.add(new ProfileVb("PROJECTS", "REPORTUSER", null));
		profileList.add(new ProfileVb("SETTLEMENT", "REPORTUSER", null));
		profileList.add(new ProfileVb("VISION", "MANAGEMENT", null));
		return profileList;
	}
}

class ProfileVb {
	private String userGrp = "";
	private String userProfile = "";
	private List<ProfileVb> children = null;

	public ProfileVb() {}
	
	public ProfileVb(String userGrp, String userProfile, List<ProfileVb> children) {
		this.userGrp = userGrp;
		this.userProfile = userProfile;
		this.children = children;
	}

	public String getUserGrp() {
		return userGrp;
	}

	public void setUserGrp(String userGrp) {
		this.userGrp = userGrp;
	}

	public String getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(String userProfile) {
		this.userProfile = userProfile;
	}

	public List<ProfileVb> getChildren() {
		return children;
	}

	public void setChildren(List<ProfileVb> children) {
		this.children = children;
	}
}