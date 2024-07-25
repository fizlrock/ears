package dev.fizlrock.ears.configurations;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.fizlrock.ears.repository.UserRepository;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;

/**
 * SecurityConfiguration
 */
@Configuration
public class SecurityConfiguration {

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService userDetails, PasswordEncoder encoder) {
    final List<AuthenticationProvider> providers = new ArrayList<>();
    // providers.add(...); // Possibly DaoAuthenticationProvider

    var dap = new DaoAuthenticationProvider();
    dap.setUserDetailsService(userDetails);
    dap.setPasswordEncoder(encoder);

    providers.add(dap);
    return new ProviderManager(providers);
  }

  @Bean
  GrpcAuthenticationReader authenticationReader() {
    final List<GrpcAuthenticationReader> readers = new ArrayList<>();
    readers.add(new BasicGrpcAuthenticationReader());
    return new CompositeGrpcAuthenticationReader(readers);
  }

  @Bean
  PasswordEncoder getPasswordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepo) {
    var users = new UserDetailsService() {
      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(username)
            .password(user.getPasswordHash())
            .roles("default")
            .build();
      }
    };

    return users;
  }

  @Bean
  GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
    final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
    // source.set(MyServiceGrpcgetMethodA(), AccessPredicate.authenticated());
    // source.set(MyServiceGrpc.getMethodB(), AccessPredicate.hasRole("ROLE_USER"));
    // source.set(MyServiceGrpc.getMethodC(), AccessPredicate.hasAllRole("ROLE_FOO", "ROLE_BAR"));
    // source.set(MyServiceGrpc.getMethodD(), (auth, call) -> "admin".equals(auth.getName()));
    source.setDefault(AccessPredicate.denyAll());
    return source;
  }

  @Bean
  AccessDecisionManager accessDecisionManager() {
    final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
    voters.add(new AccessPredicateVoter());
    return new UnanimousBased(voters);
  }

}
