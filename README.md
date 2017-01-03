# spring-searchable-jpa
Utility class for easy spring-data-jpa querying

Usage example:

```
@Entity
class User {
	@Id
	private Long id;
	@Column
	private String firstName;
	@Column
	private String lastName;
	@Column
	private float height;
	@Column
	private float weight;
}

@Repository
interface UserRepository extends JpaRepository<User, Long> {

	Page<User> findAll(Specification<User> specification, Pageable pageable);
	Page<User> findAll(Pageable pageable);
	List<User> findAll(Specification<User> specification);

}

@Service
class UserService {

	@Autowired
	private UserRepository repo;
	
	public Page<User> getUserByNameOrderingByName(String name, int pageNumber, int pageSize) {
		Searchable searchable = Searchable().builder()
			.pageNumber(pageNumber) // set page
			.pageSize(pageSize) // set page size
			.normalize(true) // setting to 'true' it removes special characters from text
			.build();
		Term nameTerm = Term.builder()
							.listTerm("firstName")
							.listTerm("lastTerm")
							.operator("like")
							.value(name)
							.build();
		searchable.terms(term);
		searchable.orderings(new Ordering("firstName", Ordering.ASC));
		
		return repo.findAll(searchable.getSpecification(), searchable.getPageable());
		// Generated an specification for an OR between "firstName" and "lastName"
	}
	
	public List<User> getUserTallerThanAndHeavierThan(float height, float weight) {
		Searchable searchable = Searchable().builder()
			.pageNumber(pageNumber) // set page
			.pageSize(pageSize) // set page size
			.normalize(true) // setting to 'true' it removes special characters from text
			.build();
		searchable.terms(new Term("height", "gt", height));
		searchable.terms(new Term("weight", "gt", weight));
		searchable.orderings(new Ordering("firstName", Ordering.ASC));
		
		return repo.findAll(searchable.getSpecification());
		// Generated an specification for an AND between "height" and "weight"
	}

}
```