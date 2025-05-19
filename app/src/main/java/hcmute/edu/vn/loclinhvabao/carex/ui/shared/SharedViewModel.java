package hcmute.edu.vn.loclinhvabao.carex.ui.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

public class SharedViewModel extends ViewModel {
    
    // LiveData for yoga days (10-day program)
    private final MutableLiveData<List<YogaDay>> yogaDays = new MutableLiveData<>();
    
    // LiveData for selected yoga day
    private final MutableLiveData<YogaDay> selectedYogaDay = new MutableLiveData<>();
    
    // LiveData for selected yoga pose
    private final MutableLiveData<YogaPose> selectedYogaPose = new MutableLiveData<>();
    
    // LiveData for user progress (completed days)
    private final MutableLiveData<Map<Integer, Boolean>> userProgress = new MutableLiveData<>();
    
    // Initialize with sample data
    public SharedViewModel() {
        initSampleData();
        initUserProgress();
    }
    
    private void initSampleData() {
        List<YogaDay> days = new ArrayList<>();
        
        // Create 10 days of yoga program
        for (int i = 1; i <= 10; i++) {
            YogaDay day = new YogaDay(i, "Day " + i + " - " + getDayTitle(i), getDayDescription(i), createPosesForDay(i));
            days.add(day);
        }
        
        yogaDays.setValue(days);
    }
    
    private void initUserProgress() {
        Map<Integer, Boolean> progress = new HashMap<>();
        // Set first day as complete for sample, others incomplete
        progress.put(1, true);
        for (int i = 2; i <= 10; i++) {
            progress.put(i, false);
        }
        userProgress.setValue(progress);
    }
    
    private String getDayTitle(int day) {
        switch (day) {
            case 1: return "Foundation";
            case 2: return "Balance";
            case 3: return "Core Strength";
            case 4: return "Flexibility";
            case 5: return "Meditation";
            case 6: return "Power Yoga";
            case 7: return "Restorative";
            case 8: return "Hip Openers";
            case 9: return "Back Bends";
            case 10: return "Full Practice";
            default: return "Yoga Practice";
        }
    }
    
    private String getDayDescription(int day) {
        switch (day) {
            case 1: return "Learn the basics of yoga poses and breathing techniques.";
            case 2: return "Focus on balance poses to improve stability and concentration.";
            case 3: return "Strengthen your core with focused yoga poses.";
            case 4: return "Improve flexibility with deep stretches.";
            case 5: return "Practice mindfulness and breathing techniques.";
            case 6: return "Build strength with dynamic flowing sequences.";
            case 7: return "Relax and restore with gentle yoga poses.";
            case 8: return "Open tight hips with targeted stretches.";
            case 9: return "Practice back bending poses safely.";
            case 10: return "Combine all learned techniques into a full yoga session.";
            default: return "Practice yoga poses and techniques.";
        }
    }
    
    private List<YogaPose> createPosesForDay(int day) {
        List<YogaPose> poses = new ArrayList<>();
        
        switch (day) {            
            case 1:
                // Day 1 - Foundation: Chair Pose, Downward Dog, and No Pose (Rest)
                poses.add(new YogaPose(1, "Chair Pose", "Utkatasana", 
                        "Stand with feet hip-width apart, bend knees as if sitting in a chair, while raising arms overhead. Keep chest lifted and weight in heels.", 
                        60, "https://www.youtube.com/watch?v=tEZhXr0FuAQ",
                        "https://images.pexels.com/photos/7625226/pexels-photo-7625226.jpeg"));
                poses.add(new YogaPose(2, "Downward Facing Dog", "Adho Mukha Svanasana", 
                        "Start on all fours, tuck toes and lift hips up and back forming an inverted V. Press through your hands, engage your legs and draw your heels toward the floor.",
                        60, "https://www.youtube.com/watch?v=j97SSGsnCAQ", 
                        "https://images.pexels.com/photos/6111609/pexels-photo-6111609.jpeg"));
                poses.add(new YogaPose(3, "No Pose (Rest)", "Vishrama", 
                        "Sit or lie comfortably. Take deep breaths and relax your body completely. Focus on your breathing.",
                        60, "https://www.youtube.com/watch?v=aXItOY0sLRY", 
                        "https://images.pexels.com/photos/6111608/pexels-photo-6111608.jpeg"));
                break;
            case 2:
                // Day 2 - Balance: Tree Pose, Warrior I, and Triangle
                poses.add(new YogaPose(4, "Tree Pose", "Vrikshasana", 
                        "Stand on one leg, place the sole of the other foot on your inner thigh or calf (not on knee), bring palms together at heart center or extend arms overhead.",
                        60, "https://www.youtube.com/watch?v=wdln9qWYloU", 
                        "https://images.pexels.com/photos/6111691/pexels-photo-6111691.jpeg"));
                poses.add(new YogaPose(5, "Warrior I", "Virabhadrasana I", 
                        "From standing, step one foot back, angling it slightly. Bend front knee to 90 degrees, raise arms overhead, and look up.",
                        60, "https://www.youtube.com/watch?v=k4qaVoAbeHM", 
                        "https://images.pexels.com/photos/6740322/pexels-photo-6740322.jpeg"));
                poses.add(new YogaPose(6, "Triangle Pose", "Trikonasana", 
                        "Stand with legs wide apart, turn right foot out 90 degrees. Extend arms, then hinge at right hip, lowering right hand toward ankle or block. Left arm extends up.",
                        60, "https://www.youtube.com/watch?v=S6gB0QHbWFE",
                        "https://images.pexels.com/photos/6453377/pexels-photo-6453377.jpeg"));
                break;
                
            case 3:
                // Day 3 - Core Strength: Chair Pose, Cobra Pose, No Pose (Rest)
                poses.add(new YogaPose(7, "Chair Pose", "Utkatasana", 
                        "Stand with feet hip-width apart, bend knees as if sitting in a chair, while raising arms overhead. Keep chest lifted and weight in heels.",
                        45, "https://www.youtube.com/watch?v=tEZhXr0FuAQ",
                        "https://images.pexels.com/photos/7625226/pexels-photo-7625226.jpeg"));                
                poses.add(new YogaPose(8, "Cobra Pose", "Bhujangasana", 
                        "Lie on stomach, palms under shoulders. Press hands down to lift chest, keeping elbows close to body, shoulders away from ears. Keep pubic bone on mat.",
                        45, "https://www.youtube.com/watch?v=zgvolE4NAH0",
                        "https://images.pexels.com/photos/4498605/pexels-photo-4498605.jpeg"));                
                poses.add(new YogaPose(9, "No Pose (Rest)", "Vishrama", 
                        "Sit or lie comfortably. Take deep breaths and relax your body completely. Focus on your breathing.",
                        30, "https://www.youtube.com/watch?v=aXItOY0sLRY", 
                        "https://images.pexels.com/photos/6111608/pexels-photo-6111608.jpeg"));
                break;                  
            case 4:
                // Day 4 - Flexibility: Triangle Pose, Downward Facing Dog, Shoulder Stand
                poses.add(new YogaPose(10, "Triangle Pose", "Trikonasana", 
                        "Stand with legs wide apart, turn right foot out 90 degrees. Extend arms, then hinge at right hip, lowering right hand toward ankle or block. Left arm extends up.",
                        60, "https://www.youtube.com/watch?v=S6gB0QHbWFE",
                        "https://images.pexels.com/photos/6453377/pexels-photo-6453377.jpeg"));
                poses.add(new YogaPose(11, "Downward Facing Dog", "Adho Mukha Svanasana", 
                        "Start on all fours, tuck toes and lift hips up and back forming an inverted V. Press through your hands, engage your legs and draw your heels toward the floor.",
                        60, "https://www.youtube.com/watch?v=j97SSGsnCAQ", 
                        "https://images.pexels.com/photos/6111609/pexels-photo-6111609.jpeg"));
                poses.add(new YogaPose(12, "Shoulder Stand", "Sarvangasana", 
                        "Lie on your back, lift legs overhead, support lower back with hands, and straighten legs upward. Keep chest close to chin and breathe steadily.",
                        45, "https://www.youtube.com/watch?v=g3wvIPXZ-Qo",
                        "https://images.pexels.com/photos/6111604/pexels-photo-6111604.jpeg"));
                break;                  
            case 5:
                // Day 5 - Meditation: No Pose (Rest), Tree Pose, Warrior II
                poses.add(new YogaPose(13, "No Pose (Rest)", "Vishrama", 
                        "Sit or lie comfortably. Take deep breaths and relax your body completely. Focus on your breathing.",
                        120, "https://www.youtube.com/watch?v=aXItOY0sLRY", 
                        "https://images.pexels.com/photos/6111608/pexels-photo-6111608.jpeg"));
                poses.add(new YogaPose(14, "Tree Pose", "Vrikshasana", 
                        "Stand on one leg, place the sole of the other foot on your inner thigh or calf (not on knee), bring palms together at heart center or extend arms overhead.",
                        60, "https://www.youtube.com/watch?v=wdln9qWYloU", 
                        "https://images.pexels.com/photos/6111691/pexels-photo-6111691.jpeg"));
                poses.add(new YogaPose(15, "Warrior II", "Virabhadrasana II", 
                        "Stand with legs wide, turn one foot out. Bend that knee to 90 degrees, extend arms parallel to floor in opposite directions.",
                        60, "https://www.youtube.com/watch?v=4Ejz7IgODlU", 
                        "https://images.pexels.com/photos/8436741/pexels-photo-8436741.jpeg"));
                break;                  
            case 6:
                // Day 6 - Power Yoga: Warrior I, Warrior II, Chair Pose
                poses.add(new YogaPose(16, "Warrior I", "Virabhadrasana I", 
                        "From standing, step one foot back, angling it slightly. Bend front knee to 90 degrees, raise arms overhead, and look up.",
                        45, "https://www.youtube.com/watch?v=k4qaVoAbeHM", 
                        "https://images.pexels.com/photos/6740322/pexels-photo-6740322.jpeg"));
                poses.add(new YogaPose(17, "Warrior II", "Virabhadrasana II", 
                        "Stand with legs wide, turn one foot out. Bend that knee to 90 degrees, extend arms parallel to floor in opposite directions.",
                        45, "https://www.youtube.com/watch?v=4Ejz7IgODlU", 
                        "https://images.pexels.com/photos/8436741/pexels-photo-8436741.jpeg"));
                poses.add(new YogaPose(18, "Chair Pose", "Utkatasana", 
                        "Stand with feet hip-width apart, bend knees as if sitting in a chair, while raising arms overhead. Keep chest lifted and weight in heels.",
                        45, "https://www.youtube.com/watch?v=tEZhXr0FuAQ",
                        "https://images.pexels.com/photos/7625226/pexels-photo-7625226.jpeg"));
                break;
            case 7:
                // Day 7 - Restorative: No Pose (Rest), Cobra Pose, Shoulder Stand
                poses.add(new YogaPose(19, "No Pose (Rest)", "Vishrama", 
                        "Sit or lie comfortably. Take deep breaths and relax your body completely. Focus on your breathing.",
                        60, "https://www.youtube.com/watch?v=aXItOY0sLRY", 
                        "https://images.pexels.com/photos/6111608/pexels-photo-6111608.jpeg"));
                poses.add(new YogaPose(20, "Cobra Pose", "Bhujangasana", 
                        "Lie on stomach, palms under shoulders. Press hands down to lift chest, keeping elbows close to body, shoulders away from ears. Keep pubic bone on mat.",
                        45, "https://www.youtube.com/watch?v=zgvolE4NAH0",
                        "https://images.pexels.com/photos/4498605/pexels-photo-4498605.jpeg"));
                poses.add(new YogaPose(21, "Shoulder Stand", "Sarvangasana", 
                        "Lie on your back, lift legs overhead, support lower back with hands, and straighten legs upward. Keep chest close to chin and breathe steadily.",
                        60, "https://www.youtube.com/watch?v=g3wvIPXZ-Qo",
                        "https://images.pexels.com/photos/6111604/pexels-photo-6111604.jpeg"));
                break;
            case 8:
                // Day 8 - Hip Openers: Warrior II, Triangle Pose, Tree Pose
                poses.add(new YogaPose(22, "Warrior II", "Virabhadrasana II", 
                        "Stand with legs wide, turn one foot out. Bend that knee to 90 degrees, extend arms parallel to floor in opposite directions.",
                        45, "https://www.youtube.com/watch?v=4Ejz7IgODlU", 
                        "https://images.pexels.com/photos/8436741/pexels-photo-8436741.jpeg"));
                poses.add(new YogaPose(23, "Triangle Pose", "Trikonasana", 
                        "Stand with legs wide apart, turn right foot out 90 degrees. Extend arms, then hinge at right hip, lowering right hand toward ankle or block. Left arm extends up.",
                        45, "https://www.youtube.com/watch?v=S6gB0QHbWFE",
                        "https://images.pexels.com/photos/6453377/pexels-photo-6453377.jpeg"));
                poses.add(new YogaPose(24, "Tree Pose", "Vrikshasana", 
                        "Stand on one leg, place the sole of the other foot on your inner thigh or calf (not on knee), bring palms together at heart center or extend arms overhead.",
                        45, "https://www.youtube.com/watch?v=wdln9qWYloU", 
                        "https://images.pexels.com/photos/6111691/pexels-photo-6111691.jpeg"));
                break;
            case 9:
                // Day 9 - Back Bends: Cobra Pose, Downward Facing Dog, Chair Pose
                poses.add(new YogaPose(25, "Cobra Pose", "Bhujangasana", 
                        "Lie on stomach, palms under shoulders. Press hands down to lift chest, keeping elbows close to body, shoulders away from ears. Keep pubic bone on mat.",
                        30, "https://www.youtube.com/watch?v=zgvolE4NAH0",
                        "https://images.pexels.com/photos/4498605/pexels-photo-4498605.jpeg"));
                poses.add(new YogaPose(26, "Downward Facing Dog", "Adho Mukha Svanasana", 
                        "Start on all fours, tuck toes and lift hips up and back forming an inverted V. Press through your hands, engage your legs and draw your heels toward the floor.",
                        30, "https://www.youtube.com/watch?v=j97SSGsnCAQ", 
                        "https://images.pexels.com/photos/6111609/pexels-photo-6111609.jpeg"));
                poses.add(new YogaPose(27, "Chair Pose", "Utkatasana", 
                        "Stand with feet hip-width apart, bend knees as if sitting in a chair, while raising arms overhead. Keep chest lifted and weight in heels.",
                        30, "https://www.youtube.com/watch?v=tEZhXr0FuAQ",
                        "https://images.pexels.com/photos/7625226/pexels-photo-7625226.jpeg"));
                poses.add(new YogaPose(28, "No Pose (Rest)", "Vishrama",
                        "Sit or lie comfortably. Take deep breaths and relax your body completely. Focus on your breathing.",
                        30, "https://www.youtube.com/watch?v=aXItOY0sLRY",
                        "https://images.pexels.com/photos/6111608/pexels-photo-6111608.jpeg"));
                break;
            case 10:
                // Day 10 - Full Practice: All the requested poses
                poses.add(new YogaPose(29, "Chair Pose", "Utkatasana", 
                        "Stand with feet hip-width apart, bend knees as if sitting in a chair, while raising arms overhead. Keep chest lifted and weight in heels.",
                        45, "https://www.youtube.com/watch?v=tEZhXr0FuAQ",
                        "https://images.pexels.com/photos/7625226/pexels-photo-7625226.jpeg"));
                poses.add(new YogaPose(30, "Cobra Pose", "Bhujangasana", 
                        "Lie on stomach, palms under shoulders. Press hands down to lift chest, keeping elbows close to body, shoulders away from ears. Keep pubic bone on mat.",
                        45, "https://www.youtube.com/watch?v=zgvolE4NAH0",
                        "https://images.pexels.com/photos/4498605/pexels-photo-4498605.jpeg"));
                poses.add(new YogaPose(31, "Downward Facing Dog", "Adho Mukha Svanasana", 
                        "Start on all fours, tuck toes and lift hips up and back forming an inverted V. Press through your hands, engage your legs and draw your heels toward the floor.",
                        45, "https://www.youtube.com/watch?v=j97SSGsnCAQ", 
                        "https://images.pexels.com/photos/6111609/pexels-photo-6111609.jpeg"));
                poses.add(new YogaPose(32, "Shoulder Stand", "Sarvangasana", 
                        "Lie on your back, lift legs overhead, support lower back with hands, and straighten legs upward. Keep chest close to chin and breathe steadily.",
                        45, "https://www.youtube.com/watch?v=g3wvIPXZ-Qo",
                        "https://images.pexels.com/photos/6111604/pexels-photo-6111604.jpeg"));
                poses.add(new YogaPose(33, "Triangle Pose", "Trikonasana", 
                        "Stand with legs wide apart, turn right foot out 90 degrees. Extend arms, then hinge at right hip, lowering right hand toward ankle or block. Left arm extends up.",
                        45, "https://www.youtube.com/watch?v=S6gB0QHbWFE",
                        "https://images.pexels.com/photos/6453377/pexels-photo-6453377.jpeg"));
                poses.add(new YogaPose(34, "Tree Pose", "Vrikshasana", 
                        "Stand on one leg, place the sole of the other foot on your inner thigh or calf (not on knee), bring palms together at heart center or extend arms overhead.",
                        45, "https://www.youtube.com/watch?v=wdln9qWYloU", 
                        "https://images.pexels.com/photos/6111691/pexels-photo-6111691.jpeg"));
                poses.add(new YogaPose(35, "Warrior I", "Virabhadrasana I", 
                        "From standing, step one foot back, angling it slightly. Bend front knee to 90 degrees, raise arms overhead, and look up.",
                        45, "https://www.youtube.com/watch?v=k4qaVoAbeHM", 
                        "https://images.pexels.com/photos/6740322/pexels-photo-6740322.jpeg"));
                poses.add(new YogaPose(36, "No Pose (Rest)", "Vishrama", 
                        "Sit or lie comfortably. Take deep breaths and relax your body completely. Focus on your breathing.",
                        45, "https://www.youtube.com/watch?v=aXItOY0sLRY", 
                        "https://images.pexels.com/photos/6111608/pexels-photo-6111608.jpeg"));
                break;
            default:
                poses.add(new YogaPose(16, "Warrior I", "Virabhadrasana I", 
                        "From standing, step one foot back, angling it slightly. Bend front knee to 90 degrees, raise arms overhead, and look up.",
                        45, "https://www.youtube.com/watch?v=k4qaVoAbeHM", 
                        "https://images.pexels.com/photos/6740322/pexels-photo-6740322.jpeg"));
                poses.add(new YogaPose(17, "Warrior II", "Virabhadrasana II", 
                        "Stand with legs wide, turn one foot out. Bend that knee to 90 degrees, extend arms parallel to floor in opposite directions.",
                        45, "https://www.youtube.com/watch?v=4Ejz7IgODlU", 
                        "https://images.pexels.com/photos/8436741/pexels-photo-8436741.jpeg"));
                poses.add(new YogaPose(29, "Bridge Pose", "Setu Bandha Sarvangasana", 
                        "Lie on back, bend knees with feet flat on floor. Press into feet to lift hips off floor. Clasp hands under back for support.",
                        30, "https://www.youtube.com/watch?v=XUcAuYd7VU0",
                        "https://images.pexels.com/photos/6465916/pexels-photo-6465916.jpeg"));
                break;
        }
        
        return poses;
    }
    
    // Getters for LiveData
    public LiveData<List<YogaDay>> getYogaDays() {
        return yogaDays;
    }
    
    public LiveData<YogaDay> getSelectedYogaDay() {
        return selectedYogaDay;
    }
    
    public LiveData<YogaPose> getSelectedYogaPose() {
        return selectedYogaPose;
    }
    
    public LiveData<Map<Integer, Boolean>> getUserProgress() {
        return userProgress;
    }
    
    // Setters
    public void selectYogaDay(YogaDay day) {
        selectedYogaDay.setValue(day);
    }
    
    public void selectYogaPose(YogaPose pose) {
        selectedYogaPose.setValue(pose);
    }
    
    public void markDayComplete(int dayNumber) {
        Map<Integer, Boolean> progress = userProgress.getValue();
        if (progress != null) {
            progress.put(dayNumber, true);
            userProgress.setValue(progress);
        }
    }
}
