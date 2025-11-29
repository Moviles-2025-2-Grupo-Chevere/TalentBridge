import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime
import matplotlib.pyplot as plt
from collections import defaultdict

# Initialize Firebase Admin SDK
# You'll need to download your service account key from Firebase Console
# and place it in your project directory
cred = credentials.Certificate('talentbridge-dev-21980-firebase-adminsdk-fbsvc-613bb6e686.json')
firebase_admin.initialize_app(cred)

# Get Firestore client
db = firestore.client()

def calculate_average_response_time_by_major():
    """
    Calculate average response time per major by matching acceptedProjects
    with projectApplications based on project_id and user_id.
    """
    # Fetch all accepted projects
    accepted_projects_ref = db.collection('acceptedProjects')
    accepted_projects = accepted_projects_ref.stream()
    
    # Fetch all project applications
    applications_ref = db.collection('projectApplications')
    applications = applications_ref.stream()
    
    # Create a dictionary to store applications by (project_id, user_id)
    applications_dict = {}
    for app in applications:
        app_data = app.to_dict()
        project_id = app_data.get('project_id')
        user_id = app_data.get('user_id')
        
        if project_id and user_id:
            key = (project_id, user_id)
            applications_dict[key] = app_data
    
    # Dictionary to store response times by major
    major_response_times = defaultdict(list)
    
    # Process accepted projects
    for accepted in accepted_projects:
        accepted_data = accepted.to_dict()
        project_id = accepted_data.get('project_id')
        user_id = accepted_data.get('user_id')
        accepted_date = accepted_data.get('acceptedDate')
        
        if not all([project_id, user_id, accepted_date]):
            continue
        
        # Find matching application
        key = (project_id, user_id)
        if key in applications_dict:
            application = applications_dict[key]
            applied_date = application.get('appliedDate')
            major = application.get('major', 'Undeclared')
            
            if applied_date:
                # Calculate time difference in minutes
                time_diff = accepted_date - applied_date
                response_time_minutes = time_diff.total_seconds() / 60
                
                # Store response time for this major
                major_response_times[major].append(response_time_minutes)
                
                print(f"Match found: {major} - {response_time_minutes:.2f} minutes")
    
    # Calculate averages
    average_times = {}
    for major, times in major_response_times.items():
        if times:
            average_times[major] = sum(times) / len(times)
            print(f"{major}: {len(times)} applications, avg {average_times[major]:.2f} minutes")
    
    return average_times

def plot_average_response_times(average_times):
    """
    Create a bar chart showing average response time per major.
    """
    if not average_times:
        print("No data to plot")
        return
    
    # Sort by average time for better visualization
    sorted_data = sorted(average_times.items(), key=lambda x: x[1], reverse=True)
    majors = [item[0] for item in sorted_data]
    avg_times = [item[1] for item in sorted_data]
    
    # Create bar chart
    plt.figure(figsize=(12, 6))
    bars = plt.bar(majors, avg_times, color='skyblue', edgecolor='navy')
    
    # Customize the plot
    plt.xlabel('Major', fontsize=12, fontweight='bold')
    plt.ylabel('Average Response Time (minutes)', fontsize=12, fontweight='bold')
    plt.title('Average Project Application Response Time by Major', 
              fontsize=14, fontweight='bold')
    plt.xticks(rotation=45, ha='right')
    plt.grid(axis='y', alpha=0.3)
    
    # Add value labels on top of bars
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'{height:.1f}',
                ha='center', va='bottom', fontsize=9)
    
    plt.tight_layout()
    plt.savefig('average_response_time_by_major.png', dpi=300, bbox_inches='tight')
    plt.close()  # Close the figure to free memory
    
    print("\nChart saved as 'average_response_time_by_major.png'")

def main():
    print("Fetching data from Firestore...")
    average_times = calculate_average_response_time_by_major()
    
    if average_times:
        print(f"\nFound {len(average_times)} majors with data")
        print("\nAverage Response Times:")
        for major, avg_time in sorted(average_times.items(), key=lambda x: x[1], reverse=True):
            print(f"  {major}: {avg_time:.2f} minutes")
        
        print("\nGenerating chart...")
        plot_average_response_times(average_times)
    else:
        print("No matching data found between acceptedProjects and projectApplications")

if __name__ == "__main__":
    main()