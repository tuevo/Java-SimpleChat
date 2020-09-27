package classes;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class MemberService {
	public static final int MEMBER_NOT_FOUND = 0;
	public static final int DUPLICATE_MEMBER = 1;
	public static final int FALIED = 3;
	public static final int SUCCESS = 4;

	private Vector<Member> members;

	public MemberService() {
		ObjectMapper mapper = new ObjectMapper();
		this.members = new Vector<Member>();
		
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(GlobalConstant.MEMBER_DB_PATH))));
			
			while(true) {
				String line = br.readLine();
				if(line == null)
					break;
				
				try {
					Member mem = mapper.readValue(line, Member.class);
					this.members.add(mem);
				} catch(JsonMappingException e) {
					System.err.println("MemberService: " + e.getMessage());
				}
			}
			
			br.close();
		} catch (Exception e) {
			System.err.println("MemberService: cannot load member list");
		}
	}
	
	public Member[] getMembers() {
		Member[] memberList = Arrays.copyOf(this.members.toArray(), this.members.size(), Member[].class);
		return memberList;
	}

	public Member findByUsername(String username) {
		for (Member m : this.members)
			if (m.getUsername().equals(username))
				return m;

		return null;
	}

	public Member add(String id, String name, String avatar, String username, String password) throws JsonProcessingException {
		Member newMember = new Member(id, name, avatar, Member.ONLINE, username, password);
		members.add(newMember);
		return this.save() == MemberService.SUCCESS ? newMember : null;
	}

	public int save() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		try {
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(GlobalConstant.MEMBER_DB_PATH))));

			for(Member m : this.members) {
				String json = mapper.writeValueAsString(m);
				bw.write(json);
				bw.newLine();
			}
			
			bw.close();
		} catch (Exception e) {
			System.err.println("MemberService::save: cannot save member list");
			return MemberService.FALIED;
		}

		return MemberService.SUCCESS;
	}
}
