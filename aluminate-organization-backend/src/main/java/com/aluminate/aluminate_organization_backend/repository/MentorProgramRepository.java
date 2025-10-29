package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MentorProgram;
import lombok.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MentorProgramRepository extends JpaRepository<MentorProgram, Long> {
    @Query("SELECT mp FROM MentorProgram mp JOIN mp.participants p WHERE p.id = :memberId")
    List<MentorProgram> findAllByParticipantId(@Param("memberId") Long memberId);

    //find all the programs by using created_by field which is user id and program participants
//    Collection<MentorProgram> findByCreatedByAndProgram(Long mentorId);
}
